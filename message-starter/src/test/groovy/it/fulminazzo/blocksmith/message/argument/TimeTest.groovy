package it.fulminazzo.blocksmith.message.argument

import net.kyori.adventure.text.Component
import spock.lang.Specification

import java.util.function.Supplier

class TimeTest extends Specification {

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
