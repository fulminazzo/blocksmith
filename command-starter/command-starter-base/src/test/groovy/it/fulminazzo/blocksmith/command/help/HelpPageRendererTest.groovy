package it.fulminazzo.blocksmith.command.help

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.CommandMessages
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.message.MessageParseContext
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.message.receiver.Receiver
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
                    .helpCommandName('help')
                    .build(),
            []
    )

    private Map<String, String> messages = [:]

    private Messenger messenger = Mock(Messenger)
    private CommandSenderWrapper<?> sender = Mock(CommandSenderWrapper)

    private CommandInput input
    private InputVisitor<?, ?> visitor = Mock(InputVisitor)

    private HelpPageRenderer renderer

    void setup() {
        messenger.getComponentOrElse(_, _, _ as String, _) >> { a ->
            def component = ComponentUtils.toComponent(messages[a[0]] ?: a[2])
            for (def arg : a[3])
                component = arg.apply(new MessageParseContext(messenger, a[1], component))
            return component
        }

        sender.receiver() >> {
            def receiver = Mock(Receiver)
            receiver.locale >> Locale.ITALY
            return receiver
        }

        input = new CommandInput()
                .addInput('root', 'help', '1')
                .advanceCursor()
                .advanceCursor()
        visitor.application >> {
            def application = Mock(ApplicationHandle)
            application.messenger >> messenger
            return application
        }
        visitor.commandSender >> sender
        visitor.input >> input

        renderer = new HelpPageRenderer(helpPage, visitor, 1)
    }

    def 'test that renderDescription of #description returns #expected'() {
        given:
        messenger.getComponentOrNull(_, _) >> description

        when:
        renderer.renderDescription()

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        description                     || expected
        null                            || ['', '']
        Component.text('Hello, world!') || ['Hello, world!', '']
    }

    def 'test that renderPermission of #permissionComponent returns #expected'() {
        given:
        messages[CommandMessages.HELP_COMMAND_PERMISSION] = permissionComponent

        when:
        renderer.renderPermission()

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        permissionComponent || expected
        null                || ['Permission: blocksmith.test.permission']
        'Perm: '            || ['Perm: blocksmith.test.permission']
    }

    def 'test that renderUsage of #usageComponent returns #expected'() {
        given:
        messages[CommandMessages.HELP_COMMAND_USAGE] = usageComponent

        when:
        renderer.renderUsage()

        then:
        renderer.lines.collect { PLAIN_SERIALIZER.serialize(it) } == expected

        where:
        usageComponent || expected
        null           || ['Usage: /test']
        'Use: '        || ['Use: /test']
    }

    def 'test that renderSubcommands with #pages, #page and #subcommands sets lines to #expected'() {
        given:
        def helpPage = Mock(HelpPage)
        helpPage.getSubcommandsPages(_, _) >> pages
        helpPage.getSubcommandsPage(_, _, _) >> {
            subcommands == 0
                    ? []
                    : (1..subcommands).collect {
                def data = Mock(HelpPage.CommandData)
                data.name >> "subcommand$it"
                data.description >> "subcommand$it description"
                data.permission >> new PermissionInfo('blocksmith', "subcommand$it", Permission.Grant.ALL)
                data.usage >> "/test subcommand$it"
                data.helpCommandName >> 'help'
                return data
            }
        }

        and:
        input.advanceCursor().advanceCursor()
        def renderer = new HelpPageRenderer(helpPage, visitor, page)

        and:
        messages[CommandMessages.HELP_COMMAND_SUBCOMMAND_FORMAT] = 'subcommand'

        when:
        renderer.renderSubcommands()

        then:
        renderer.lines.collect { ComponentUtils.toString(it) } == expected

        where:
        pages | page | subcommands || expected
        1     | 1    | 1           || ['<click:run_command:\'root subcommand1 help\'>subcommand', '', '']
        1     | 1    | 2           || ['<click:run_command:\'root subcommand1 help\'>subcommand',
                                       '<click:run_command:\'root subcommand2 help\'>subcommand',
                                       '']
        1     | 1    | 3           || ['<click:run_command:\'root subcommand1 help\'>subcommand',
                                       '<click:run_command:\'root subcommand2 help\'>subcommand',
                                       '<click:run_command:\'root subcommand3 help\'>subcommand']
        1     | 2    | 1           || ['<click:run_command:\'root subcommand1 help\'>subcommand', '', '']
        0     | 0    | 0           || ['\n  <red>(none)</red>\n ']
    }

    def 'test that renderSubcommand correctly renders subcommand'() {
        given:
        def command = HelpPage.CommandData.builder()
                .name('test')
                .description('command.test.description')
                .permission(new PermissionInfo('blocksmith', 'test', Permission.Grant.ALL))
                .usage('/test test <something>')
                .helpCommandName('help')
                .build()

        and:
        messages[command.description] = actualDescription

        when:
        renderer.renderSubcommand(command)

        then:
        ComponentUtils.toString(renderer.lines[0]) == '<click:run_command:\'root test help\'>' +
                '<hover:show_text:\'' +
                '<white>/test test \\\\<something></white><br>' +
                '<gray>blocksmith.test</gray>\n\n' +
                '<aqua>Click for more information' +
                '\'>' +
                "<white>test</white> <dark_gray>-</dark_gray> <gray>$description"

        where:
        actualDescription                                                || description
        'Test description'                                               || 'Test description'
        'Super long test description to ensure it is properly truncated' || 'Super long test description to ensure it is properl</gray>...'
    }

    def 'test that parseFillerComponent of #component returns #expected'() {
        when:
        def actual = renderer.parseFillerComponent(ComponentUtils.toComponent(component))

        then:
        ComponentUtils.toString(actual) == expected

        where:
        component                             || expected
        'Hello, world!'                       || 'Hello, world!'
        '%filler%'                            || '<strikethrough><gold>-----------------------------------------------------'
        '%filler%A%filler%'                   || '<strikethrough><gold>--------------------------</gold></strikethrough>A<strikethrough><gold>--------------------------'
        '%filler%A%filler%B%filler%'          ||
                '<strikethrough><gold>-----------------</gold></strikethrough>A<strikethrough><gold>-----------------</gold></strikethrough>B<strikethrough><gold>-----------------'
        '%filler%A%filler%B%filler%C%filler%' ||
                '<strikethrough><gold>------------</gold></strikethrough>A<strikethrough><gold>------------</gold></strikethrough>B<strikethrough><gold>------------</gold></strikethrough>C<strikethrough><gold>------------'
    }

    def 'test that getComponentOrFillers of #string and #condition returns self'() {
        given:
        def component = Component.text(string)

        when:
        def actual = renderer.getComponentOrFillers(condition, component)

        then:
        actual == component

        where:
        string  | condition
        ''      | false
        ''      | true
        'Hello' | true
    }

    def 'test that getComponentOrFillers returns a component of fillers so that the length matches as much as possible the original'() {
        given:
        def component = Component.text(string)

        when:
        def actualComponent = renderer.getComponentOrFillers(false, component)
        def actual = PLAIN_SERIALIZER.serialize(actualComponent)

        then:
        MinecraftFontWidth.getWidth(actual) - MinecraftFontWidth.getWidth(string) <= 5

        where:
        string << [
                'Hello',
                'Hello, world!',
                'Goodbye, mars!',
                '[<<]',
                '@@',
                '@@@@@',
                '@@@@@@@',
                '@@@@@@@@'
        ]
    }

    /*
     * UTILITIES
     */

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
