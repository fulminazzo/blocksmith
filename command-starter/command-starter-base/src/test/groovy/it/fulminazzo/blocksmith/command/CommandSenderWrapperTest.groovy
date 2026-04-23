package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.scheduler.Scheduler
import it.fulminazzo.blocksmith.scheduler.Task
import it.fulminazzo.blocksmith.scheduler.TaskBuilder
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
        def sender = new MockCommandSenderWrapper(new CommandSender('Alex'))

        and:
        def result = new AtomicReference()

        when:
        sender.sync { result.set(it.name) }

        then:
        result.get() == sender.name

        cleanup:
        mocked.close()
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
        false  || ArgumentParsers.CONSOLE_COMMAND_NAME
    }

}
