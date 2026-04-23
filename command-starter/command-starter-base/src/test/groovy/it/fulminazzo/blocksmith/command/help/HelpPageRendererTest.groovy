package it.fulminazzo.blocksmith.command.help

import net.kyori.adventure.text.Component
import spock.lang.Specification

class HelpPageRendererTest extends Specification {

    def 'test that truncate of short string does not truncate'() {
        given:
        def component = Component.text('Hello, world!')

        when:
        def actual = HelpPageRenderer.truncate('', component)

        then:
        actual == component
    }

    def 'test that truncate of long string truncates and sets hover event'() {
        given:
        def component = Component.text('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@')

        when:
        def actual = HelpPageRenderer.truncate('', component)

        then:
        actual != component

        and:
        HelpPageRenderer.PLAIN_SERIALIZER.serialize(actual) == '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@...'

        and:
        def hoverEvent = actual.hoverEvent()
        hoverEvent != null
        hoverEvent.value() == component
    }

    def 'test that getMaxTruncationLength of #string returns #expected'() {
        when:
        def actual = HelpPageRenderer.getMaxTruncationLength(string)

        then:
        actual == expected

        where:
        string                                           || expected
        '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@'  || -1
        '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@' || 44
    }

    def 'test that getMaxLength of #string returns #expected'() {
        when:
        def actual = HelpPageRenderer.getMaxLength(string)

        then:
        actual == expected

        where:
        string                                           || expected
        '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@'  || -1
        '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@' || 44
    }

}
