package it.fulminazzo.blocksmith.message.argument

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.message.MessageParseContext
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException
import it.fulminazzo.blocksmith.message.provider.MessageProvider
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import spock.lang.Specification

import java.util.function.Supplier

@Slf4j
class TimeTest extends Specification {

    def 'test that apply correctly applies with #format'() {
        given:
        def provider = Mock(MessageProvider)
        provider.getMessage(_, _) >> { a ->
            def code = a[0]
            if (code == 'recursive') return Component.text('(%seconds% {second|seconds})')
            else throw new MessageNotFoundException(code, a[1])
        }

        and:
        def messenger = new Messenger(log)
        messenger.messageProvider = provider

        and:
        def component = Component.text("Elapsed: %time%")

        and:
        def t = Time.of('time', format, 1000L)

        when:
        def actual = t.apply(new MessageParseContext(messenger, Locale.ITALY, component))

        then:
        ComponentUtils.toString(actual) == 'Elapsed: 1 second'

        where:
        format << ['(%seconds% {second|seconds})', 'recursive']
    }

    def 'test that Time of with #arguments correctly creates time argument'() {
        when:
        def time = Time.of(*arguments)

        then:
        time.placeholder == 'time'
        time.timeFormat == 'general.time-format'
        time.timeSupplier.get() == 1L

        where:
        arguments << [
                [1L],
                [(Supplier<Long>) () -> 1],
                [Component.text('time'), 1L],
                ['time', 1L],
                [Component.text('time'), (Supplier<Long>) () -> 1],
                ['time', (Supplier<Long>) () -> 1],
                [Component.text('time'), Component.text('general.time-format'), 1L],
                ['time', Component.text('general.time-format'), 1L],
                [Component.text('time'), Component.text('general.time-format'), (Supplier<Long>) () -> 1],
                ['time', Component.text('general.time-format'), (Supplier<Long>) () -> 1],
                [Component.text('time'), 'general.time-format', 1L],
                ['time', 'general.time-format', 1L],
                [Component.text('time'), 'general.time-format', (Supplier<Long>) () -> 1],
                ['time', 'general.time-format', (Supplier<Long>) () -> 1],
        ]
    }

}
