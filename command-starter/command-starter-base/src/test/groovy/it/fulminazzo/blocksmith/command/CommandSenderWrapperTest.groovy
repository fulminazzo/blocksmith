package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.message.receiver.Receiver
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactory
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import it.fulminazzo.blocksmith.scheduler.Scheduler
import it.fulminazzo.blocksmith.scheduler.Task
import it.fulminazzo.blocksmith.scheduler.TaskBuilder
import net.kyori.adventure.audience.Audience
import org.mockito.Mockito
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference

class CommandSenderWrapperTest extends Specification {

    def 'test that sync schedules and runs task synchronously'() {
        given:
        def mocked = Mockito.mockStatic(Scheduler)

        and:
        mocked.when { Scheduler.schedule(Mockito.any(), Mockito.any()) }.thenAnswer { i ->
            def args = i.arguments

            def builder = Mock(TaskBuilder)
            builder.run() >> {
                def task = Mock(Task)
                args[1].accept(task)
                return task
            }
            return builder
        }

        and:
        def sender = new MockCommandSenderWrapper(new Player('Alex'))

        and:
        def result = new AtomicReference()

        when:
        sender.sync { result.set(it.name) }

        then:
        result.get() == sender.name

        cleanup:
        mocked.close()
    }

    def 'test that sendMessage calls on audience'() {
        given:
        def audience = Mock(Audience)
        def receiver = Mock(Receiver)
        receiver.audience() >> audience

        and:
        def receiverFactory = Mock(ReceiverFactory)
        receiverFactory.create(_) >> receiver

        and:
        def receiverFactories = Mockito.mockStatic(ReceiverFactories)
        receiverFactories.when { ReceiverFactories.get(Mockito.any(), Mockito.any()) }.thenReturn(receiverFactory)

        and:
        def sender = new MockCommandSenderWrapper(new CommandSender())

        when:
        sender.sendMessage(message)

        then:
        1 * audience.sendMessage(_) >> { a ->
            def m = ComponentUtils.toString(a[0])
            assert m == 'Hello, world!'
        }

        cleanup:
        receiverFactories.close()

        where:
        message << ['Hello, world!', ComponentUtils.toComponent('Hello, world!')]
    }

    def 'test that getName with #player returns #expected'() {
        given:
        def sender = new MockCommandSenderWrapper(player
                ? new Player('Steve')
                : new ConsoleCommandSender()
        )

        when:
        def name = sender.name

        then:
        name == expected

        where:
        player || expected
        true   || 'Steve'
        false  || CommandSenderWrapper.CONSOLE_COMMAND_NAME
    }

}
