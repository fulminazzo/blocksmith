package it.fulminazzo.blocksmith.command.help

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import spock.lang.Specification

class HelpPageRendererTest extends Specification {
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String MAX_CHARS = '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@'
    private static final String TRUNCATED_CHARS = '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@...'

    private static final HelpPage helpPage = new HelpPage(
            HelpPage.CommandData.builder()
                    .name('test')
                    .description('test.description')
                    .permission(new PermissionInfo('blocksmith', 'test.permission', Permission.Grant.NONE))
                    .usage('/test')
                    .build(),
            []
    )

    def 'test that renderDescription of #description returns #expected'() {
        given:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull(_, _) >> description

        and:
        def renderer = new HelpPageRenderer(helpPage)

        when:
        renderer.renderDescription(messenger, Locale.ITALY)

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        description                     || expected
        null                            || ['', '']
        Component.text('Hello, world!') || ['Hello, world!', '']
    }

    def 'test that renderPermission of #permissionComponent returns #expected'() {
        given:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull(_, _) >> permissionComponent

        and:
        def renderer = new HelpPageRenderer(helpPage)

        when:
        renderer.renderPermission(messenger, Locale.ITALY)

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        permissionComponent      || expected
        null                     || ['Permission: blocksmith.test.permission']
        Component.text('Perm: ') || ['Perm: blocksmith.test.permission']
    }

    def 'test that renderUsage of #usageComponent returns #expected'() {
        given:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull(_, _) >> usageComponent

        and:
        def renderer = new HelpPageRenderer(helpPage)

        when:
        renderer.renderUsage(messenger, Locale.ITALY)

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        usageComponent          || expected
        null                    || ['Usage: /test']
        Component.text('Use: ') || ['Use: /test']
    }

    def 'test that renderSubcommand correctly renders subcommand'() {
        given:
        def renderer = new HelpPageRenderer(helpPage)

        and:
        def command = HelpPage.CommandData.builder()
                .name('test')
                .description('command.test.description')
                .permission(new PermissionInfo('blocksmith', 'test', Permission.Grant.ALL))
                .usage('/test test <something>')
                .build()

        and:
        def messenger = Mock(Messenger)
        messenger.getComponentOrNull('command.test.description', _, _) >> Component.text(actualDescription)

        when:
        renderer.renderSubcommand(messenger, Locale.ITALY, command)

        then:
        ComponentUtils.toString(renderer.lines[0]) == '<click:run_command:\'%command%\'>' +
                '<hover:show_text:\'' +
                '<white>/test test \\\\<something></white>\\\\\\\\n' +
                '<gray>blocksmith.test</gray>\\\\\\\\n\\\\\\\\n' +
                '<aqua>Click for more information' +
                '\'>' +
                "<white>test</white> <dark_gray>-</dark_gray> <gray>$description"

        where:
        actualDescription                                                || description
        'Test description'                                               || 'Test description'
        'Super long test description to ensure it is properly truncated' || 'Super long test description to ensure it is properl</gray>...'
    }

    def 'test that formatAndFill correctly formats component'() {
        given:
        def renderer = new HelpPageRenderer(helpPage)

        when:
        def component = renderer.formatAndFill('Title', Mock(Messenger), Locale.ITALY)

        then:
        ComponentUtils.toString(component) == '<strikethrough><gold>------------------------</gold></strikethrough>' +
                ' Title ' +
                '<strikethrough><gold>------------------------'
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
