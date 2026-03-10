package it.fulminazzo.blocksmith.message.argument

import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import spock.lang.Specification

class PlaceholderTest extends Specification {

    def 'test that apply correctly applies with #placeholder and #value'() {
        given:
        def expected = 'Hello, Alex!'

        and:
        def component = Component.text('Hello, %name%!')

        and:
        def p = Placeholder.of(placeholder, value)

        when:
        def actual = p.apply(component)

        then:
        ComponentUtils.toString(actual) == expected

        where:
        placeholder            | value
        'name'                 | 'Alex'
        Component.text('name') | 'Alex'
        Component.text('name') | Component.text('Alex')
        'name'                 | Component.text('Alex')
    }

}
