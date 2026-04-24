package it.fulminazzo.blocksmith.message

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ServerApplication
import it.fulminazzo.blocksmith.message.argument.Placeholder
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException
import it.fulminazzo.blocksmith.message.provider.MessageProvider
import it.fulminazzo.blocksmith.message.receiver.PlayerReceiverFactory
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactory
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.translation.GlobalTranslator
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

@Slf4j
class MessengerTest extends Specification {

    private Player player
    private MessageProvider provider
    private Messenger messenger

    private Map<String, Component> messages = [:]

    void setup() {
        ReceiverFactories.factories.add((Supplier<ReceiverFactory>) () -> new PlayerReceiverFactory())

        player = new Player('Luke')

        provider = Mock(MessageProvider)
        provider.getMessage(_, _) >> { a ->
            def message = messages[a[0]]
            if (message == null) throw new MessageNotFoundException(a[0], a[1])
            return message
        }

        messages['prefix'] = Component.text('blocksmith | ')

        def application = Mock(ServerApplication)
        application.logger() >> log
        application.lowercaseName() >> 'blocksmith'

        messenger = new Messenger(application).setMessageProvider(provider)
    }

    def 'test that adventure translation system works'() {
        given:
        messages['greeting'] = Component.text('Hello, world!')

        and:
        def translator = GlobalTranslator.translator()

        expect:
        translator.sources.empty

        when:
        messenger.setupTranslator()

        then:
        !translator.sources.empty

        when:
        def component = Component.translatable('blocksmith.greeting')

        and:
        def text = translator.translate(component, Locale.US).content

        then:
        text == 'Hello, world!'

        and:
        translator.translate(component.key(), Locale.US) == null

        when:
        component = Component.translatable('greeting')

        and:
        text = translator.translate(component, Locale.US)

        then:
        text == null

        when:
        messenger.removeTranslator()

        then:
        translator.sources.empty

        when:
        component = Component.translatable('blocksmith.greeting')

        and:
        text = translator.translate(component, Locale.US)

        then:
        text == null
    }

    def 'test that setupTranslator throws if already initialized'() {
        given:
        messenger.setupTranslator()

        when:
        messenger.setupTranslator()

        then:
        thrown(IllegalStateException)

        cleanup:
        def translator = GlobalTranslator.translator()
        translator.removeSource(translator.sources[0])
    }

    def 'test that removeTranslator throws if not initialized'() {
        when:
        messenger.removeTranslator()

        then:
        thrown(IllegalStateException)
    }

    def 'test that broadcastTitle correctly converts and sends message to all receivers'() {
        given:
        def expected = Component.text('[Broadcast] [Title] Hello, world!')

        and:
        messages['message'] = expected

        and:
        Player.ALL_PLAYERS.each { it.locale = Locale.ITALY }

        when:
        messenger.broadcastTitle('message', 'message')

        then:
        for (def player : Player.ALL_PLAYERS) {
            println player

            def lastTitle = player.lastTitle

            assert lastTitle[TitlePart.TITLE] == expected

            assert lastTitle[TitlePart.SUBTITLE] == expected

            assert lastTitle[TitlePart.TIMES] == Title.Times.times(
                    Duration.of(1L, ChronoUnit.SECONDS),
                    Duration.of(2L, ChronoUnit.SECONDS),
                    Duration.of(1L, ChronoUnit.SECONDS)
            )
        }
    }

    def 'test that broadcastTitle with times correctly converts and sends message to all receivers'() {
        given:
        def expected = Component.text('[Broadcast] [Title] [Timed] Hello, world!')

        and:
        def times = Title.Times.times(
                Duration.of(2L, ChronoUnit.SECONDS),
                Duration.of(4L, ChronoUnit.SECONDS),
                Duration.of(2L, ChronoUnit.SECONDS)
        )

        and:
        messages['message'] = expected

        and:
        Player.ALL_PLAYERS.each { it.locale = Locale.ITALY }

        when:
        messenger.broadcastTitle('message', 'message', times)

        then:
        for (def player : Player.ALL_PLAYERS) {
            println player

            def lastTitle = player.lastTitle

            assert lastTitle[TitlePart.TITLE] == expected

            assert lastTitle[TitlePart.SUBTITLE] == expected

            assert lastTitle[TitlePart.TIMES] == times
        }
    }

    def 'test that broadcastActionBar correctly converts and sends message to all receivers'() {
        given:
        def expected = '[Broadcast] [Actionbar] Hello, world!'

        and:
        messages['message'] = Component.text(expected)

        and:
        Player.ALL_PLAYERS.each { it.locale = Locale.ITALY }

        when:
        messenger.broadcastActionBar('message')

        then:
        for (def player : Player.ALL_PLAYERS) {
            def lastMessage = player.lastMessage
            println player
            assert lastMessage != null
            assert ComponentUtils.toString(lastMessage) == expected
        }
    }

    def 'test that broadcastMessage correctly converts and sends message to all receivers'() {
        given:
        def expected = '[Broadcast] Hello, world!'

        and:
        messages['message'] = Component.text('[Broadcast] Hello, %what%!')

        and:
        Player.ALL_PLAYERS.each { it.locale = Locale.ITALY }

        when:
        messenger.broadcastMessage('message', Placeholder.of('what', 'world'))

        then:
        for (def player : Player.ALL_PLAYERS) {
            def lastMessage = player.lastMessage
            println player
            assert lastMessage != null
            assert ComponentUtils.toString(lastMessage) == expected
        }
    }

    def 'test that sendTitle correctly converts and sends message'() {
        given:
        def expected = Component.text('[Title] Hello, world!')

        and:
        messages['message'] = expected

        and:
        player.locale = Locale.ITALY

        when:
        messenger.sendTitle(player, titleCode, subtitleCode)

        and:
        def lastTitle = player.lastTitle

        then:
        lastTitle[TitlePart.TITLE] == titleCode == null ? Component.empty() : expected

        and:
        lastTitle[TitlePart.SUBTITLE] == subtitleCode == null ? Component.empty() : expected

        and:
        lastTitle[TitlePart.TIMES] == (titleCode == null && subtitleCode == null)
                ? null
                : Title.Times.times(
                Duration.of(1L, ChronoUnit.SECONDS),
                Duration.of(2L, ChronoUnit.SECONDS),
                Duration.of(1L, ChronoUnit.SECONDS)
        )

        where:
        titleCode | subtitleCode
        null      | null
        null      | 'message'
        'message' | null
        'message' | 'message'
    }

    def 'test that sendActionBar correctly converts and sends message'() {
        given:
        def expected = '[ActionBar] Hello, world!'

        and:
        messages['message'] = Component.text(expected)

        and:
        player.locale = Locale.ITALY

        when:
        messenger.sendActionBar(player, 'message')

        then:
        def lastMessage = player.lastMessage
        lastMessage != null
        ComponentUtils.toString(lastMessage) == expected
    }

    def 'test that sendMessage correctly converts and sends message'() {
        given:
        def expected = 'Hello, world!'

        and:
        messages['message'] = Component.text('Hello, %what%!')

        and:
        player.locale = Locale.ITALY

        when:
        messenger.sendMessage(player, 'message', Placeholder.of('what', 'world'))

        then:
        def lastMessage = player.lastMessage
        lastMessage != null
        ComponentUtils.toString(lastMessage) == expected
    }

    def 'test that sendMessage does not throw on not found'() {
        when:
        messenger.sendMessage(player, 'message')

        then:
        noExceptionThrown()
    }

    def 'test that getComponent calls on provider'() {
        given:
        def code = 'message'
        def locale = Locale.default

        when:
        messenger.getComponent(code, locale)

        then:
        1 * provider.getMessage(code, locale) >> Mock(Component)
    }

    def 'test that getMessageProvider with no provider throws'() {
        given:
        messenger.messageProvider = null

        when:
        messenger.messageProvider

        then:
        thrown(IllegalStateException)
    }

}
