package it.fulminazzo.blocksmith.command.help

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import spock.lang.Specification

class HelpPageRendererTest extends Specification {
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String MAX_CHARS = '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@'
    private static final String TRUNCATED_CHARS = '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@...'

    def 'test that renderDescription of #description returns #expected'() {
        given:
        def node = new LiteralNode('test')
        node.commandInfo = new CommandInfo(
                'test.description',
                new PermissionInfo(null, 'test.permission', Permission.Grant.NONE)
        )

        and:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull(_, _) >> description

        and:
        def renderer = new HelpPageRenderer(node)

        when:
        renderer.renderDescription(messenger, Locale.ITALY)

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        description                     || expected
        null                            || ['', '', '']
        Component.text('Hello, world!') || ['Hello, world!', '', '']
    }

    def 'test that renderUsage of #usageComponent returns #expected'() {
        given:
        def node = new LiteralNode('test')
        node.commandInfo = new CommandInfo(
                'test.description',
                new PermissionInfo(null, 'test.permission', Permission.Grant.NONE)
        )

        and:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull(_, _) >> usageComponent

        and:
        def renderer = new HelpPageRenderer(node)

        when:
        renderer.renderUsage(messenger, Locale.ITALY)

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        usageComponent            || expected
        null                      || ['/test']
        Component.text('Usage: ') || ['Usage: /test']
    }


    def 'test that truncateLines of #string returns #expected'() {
        given:
        def component = Component.text(string)

        when:
        def actual = HelpPageRenderer.truncateLines(component, 3)
                .collect { PLAIN_SERIALIZER.serialize(it) }

        then:
        actual == expected

        where:
        string                                         || expected
        ''                                             || []
        'Hello, world!'                                || ['Hello, world!']
        MAX_CHARS                                      || [MAX_CHARS]
        "$MAX_CHARS Hello, world!"                     || [MAX_CHARS, ' Hello, world!']
        MAX_CHARS * 2                                  || [MAX_CHARS, MAX_CHARS]
        "$MAX_CHARS$MAX_CHARS Hello, world!"           || [MAX_CHARS, MAX_CHARS, ' Hello, world!']
        MAX_CHARS * 3                                  || [MAX_CHARS, MAX_CHARS, MAX_CHARS]
        "$MAX_CHARS$MAX_CHARS$MAX_CHARS Hello, world!" || [MAX_CHARS, MAX_CHARS, TRUNCATED_CHARS]
        MAX_CHARS * 4                                  || [MAX_CHARS, MAX_CHARS, TRUNCATED_CHARS]
    }

    def 'test that truncate of long string truncates and sets hover event'() {
        given:
        def component = Component.text("$MAX_CHARS@")

        when:
        def actual = HelpPageRenderer.truncate('', component)

        then:
        actual != component

        and:
        PLAIN_SERIALIZER.serialize(actual) == TRUNCATED_CHARS

        and:
        def hoverEvent = actual.hoverEvent()
        hoverEvent != null
        hoverEvent.value() == component
    }

    def 'test that truncate of short string does not truncate'() {
        given:
        def component = Component.text(MAX_CHARS)

        when:
        def actual = HelpPageRenderer.truncate('', component)

        then:
        actual == component
    }

    def 'test that getMaxTruncationLength of #string returns #expected'() {
        when:
        def actual = HelpPageRenderer.getMaxTruncationLength(string)

        then:
        actual == expected

        where:
        string        || expected
        MAX_CHARS     || -1
        "$MAX_CHARS@" || 44
    }

    def 'test that getMaxLength of #string returns #expected'() {
        when:
        def actual = HelpPageRenderer.getMaxLength(string)

        then:
        actual == expected

        where:
        string        || expected
        MAX_CHARS     || -1
        "$MAX_CHARS@" || 44
    }

}
