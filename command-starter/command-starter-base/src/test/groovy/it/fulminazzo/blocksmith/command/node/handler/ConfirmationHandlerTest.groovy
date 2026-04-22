package it.fulminazzo.blocksmith.command.node.handler

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Confirm
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionException
import it.fulminazzo.blocksmith.command.visitor.execution.CommandExecutionVisitor
import it.fulminazzo.blocksmith.reflect.Reflect
import it.fulminazzo.blocksmith.structure.task.PendingTaskManager
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ConfirmationHandlerTest extends Specification {
    private Confirm confirmationInfo
    private PendingTaskManager<Object> confirmationManager

    private ConfirmationHandler handler

    private CommandSenderWrapper<?> sender

    void setup() {
        confirmationInfo = Mock(Confirm)
        confirmationInfo.timeout() >> 250
        confirmationInfo.unit() >> TimeUnit.MILLISECONDS
        confirmationInfo.confirmWord() >> 'confirm'
        confirmationInfo.cancelWord() >> 'cancel'

        handler = new ConfirmationHandler(confirmationInfo)
        confirmationManager = handler.confirmationManager

        sender = new MockCommandSenderWrapper(new CommandSender('Steve'))
    }

    def 'test that ConfirmationHandler does not throw on CommandExecutionException'() {
        given:
        def input = new CommandInput().addInput('delete', 'Fulminazzo')

        and:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.input >> input

        when:
        handler.handleExecution(
                visitor,
                () -> {
                    throw new CommandExecutionException('error.mock-error')
                }
        )

        then:
        noExceptionThrown()

        when:
        Reflect.on(input).get('input').invoke('clear')
        input.addInput('delete', 'confirm')

        and:
        def result = handler.checkConfirmationKeywords(visitor)

        then:
        result

        and:
        noExceptionThrown()

        and:
        1 * visitor.handleCommandExecutionException(_) >> { a ->
            assert a[0].message == 'error.mock-error'
        }
    }

    /*
     * checkConfirmationKeywords
     */

    def 'test that handleExecution correctly registers task'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender

        and:
        def executed = new AtomicBoolean()

        when:
        handler.handleExecution(visitor, () -> executed.set(true))

        then:
        confirmationManager.execute(sender.id)

        then:
        executed.get()
    }

    def 'test that if input is last when checkConfirmationKeywords then nothing happens'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.input >> new CommandInput().addInput('delete')

        expect:
        !handler.checkConfirmationKeywords(visitor)
    }

    def 'test that checkConfirmationKeywords successful execution returns true'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.input >> new CommandInput().addInput('delete', 'confirm')

        and:
        def executed = new AtomicBoolean()

        and:
        confirmationManager.register(sender.id, 250, () -> executed.set(true))

        expect:
        handler.checkConfirmationKeywords(visitor)

        and:
        executed.get()
    }

    def 'test that checkConfirmationKeywords throws #message on #keyword and #wait'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.input >> new CommandInput().addInput('delete', keyword)

        and:
        if (wait > 0) {
            confirmationManager.register(sender.id, 250, () -> { })
            sleep(wait)
        }

        when:
        handler.checkConfirmationKeywords(visitor)

        then:
        def e = thrown(CommandExecutionException)
        e.message == message

        where:
        keyword   | wait || message
        'confirm' | 0    || 'error.no-pending-action'
        'confirm' | 300  || 'error.pending-action-expired'
        'cancel'  | 1    || 'success.pending-action-cancelled'
        'cancel'  | 0    || 'error.no-pending-action'
        'cancel'  | 300  || 'error.pending-action-expired'
    }

    def 'test that checkConfirmationKeywords does not throw for other keyword'() {
        given:
        def visitor = Mock(CommandExecutionVisitor)
        visitor.commandSender >> sender
        visitor.input >> new CommandInput().addInput('delete', 'someone')

        expect:
        !handler.checkConfirmationKeywords(visitor)
    }


    def 'test that getCompletions returns confirm and cancel words'() {
        given:
        def confirmationInfo = Mock(Confirm)
        confirmationInfo.confirmWord() >> 'yes'
        confirmationInfo.cancelWord() >> 'no'

        and:
        def handler = new ConfirmationHandler(confirmationInfo)

        and:
        def visitor = Mock(InputVisitor)

        expect:
        handler.getCompletions(visitor).sort() == ['yes', 'no'].sort()
    }

    def 'test that confirmation timeout returns correct timeout'() {
        expect:
        handler.confirmationTimeout == Duration.ofMillis(250)
    }

}
