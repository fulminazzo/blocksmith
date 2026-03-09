package it.fulminazzo.blocksmith.message.argument

import net.kyori.adventure.text.Component
import spock.lang.Specification

class PlaceholderTest extends Specification {

    def 'test that apply correctly applies with #placeholder and #value'() {
        given:
        def expected = Component.text('Hello, Alex!')

        and:
        def component = Component.text('Hello, %name%!')

        and:
        def p = Placeholder.of(placeholder, value)

        when:
        def actual = p.apply(component)

        then:
        actual == expected

        where:
        placeholder            | value
        'name'                 | 'Alex'
        Component.text('name') | 'Alex'
        'name'                 | Component.text('Alex')
        Component.text('name') | Component.text('Alex')
    }

}
