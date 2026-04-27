package it.fulminazzo.blocksmith.message.argument

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ServerApplication
import it.fulminazzo.blocksmith.message.MessageParseContext
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException
import it.fulminazzo.blocksmith.message.provider.MessageProvider
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import spock.lang.Specification

@Slf4j
class PlaceholderTest extends Specification {

    private Messenger messenger

    void setup() {
        def provider = Mock(MessageProvider)
        provider.getMessage(_, _) >> { a ->
            def code = a[0]
            if (code == 'second') return Component.text('Alex')
            else throw new MessageNotFoundException(code, a[1])
        }

        def application = Mock(ServerApplication)
        application.logger() >> log
        messenger = new Messenger(application)
        messenger.messageProvider = provider
    }

    def 'test that apply correctly applies with #placeholder and #value'() {
        given:
        def expectedString = "Hello, $expected!"

        and:
        def component = Component.text('Hello, %name%!')

        and:
        def p = Placeholder.of(placeholder, value)

        when:
        def actual = p.apply(new MessageParseContext(messenger, Locale.ITALY, component))

        then:
        ComponentUtils.toString(actual) == expectedString

        where:
        placeholder            | value                  || expected
        'name'                 | 'Alex'                 || 'Alex'
        'name'                 | null                   || 'null'
        Component.text('name') | 'Alex'                 || 'Alex'
        Component.text('name') | null                   || 'null'
        Component.text('name') | Component.text('Alex') || 'Alex'
        'name'                 | Component.text('Alex') || 'Alex'
    }

    def 'test that apply correctly applies recursively placeholder'() {
        given:
        def component = Component.text('Hello, %name%!')

        and:
        def p = Placeholder.of('name', 'second')

        when:
        def actual = p.apply(new MessageParseContext(messenger, Locale.ITALY, component))

        then:
        ComponentUtils.toString(actual) == 'Hello, Alex!'
    }

}
