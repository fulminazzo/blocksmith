package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.message.receiver.Receiver
import it.fulminazzo.blocksmith.reflect.Reflect
import it.fulminazzo.blocksmith.scheduler.Scheduler
import it.fulminazzo.blocksmith.scheduler.Task
import it.fulminazzo.blocksmith.scheduler.TaskBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.mockito.Mockito
import spock.lang.Specification

import java.lang.reflect.Method
import java.lang.reflect.Modifier
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

    def 'test that #method returns CommandSenderWrapper'() {
        given:
        def sender = new MockCommandSenderWrapper(new CommandSender())
        def reflect = Reflect.on(sender)

        and:
        def actualMethod = reflect.getMethod(method.name, method.parameterTypes)

        expect:
        actualMethod.returnType == CommandSenderWrapper

        when:
        def params = actualMethod.parameterTypes.collect {
            switch (it) {
                case String: return 'Hello, world!'
                case Component: return Component.text('Hello, world!')
                case Title.Times: return Receiver.DEFAULT_TIMES
                default: return null
            }
        }

        and:
        def actual = reflect.invoke(method, *params)

        then:
        actual.get() == sender

        where:
        method << getReturnTypeMethods(Receiver)
    }

    /**
     * Gets all the instance methods in the type that return the type itself.
     *
     * @param type the type to get the methods from
     * @return the methods
     */
    static List<Method> getReturnTypeMethods(final Class<?> type) {
        return Reflect.on(type).getMethods { !Modifier.isStatic(it.modifiers) && it.returnType == type }
    }

}
