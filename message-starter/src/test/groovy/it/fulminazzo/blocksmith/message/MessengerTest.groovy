package it.fulminazzo.blocksmith.message

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.message.argument.Placeholder
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException
import it.fulminazzo.blocksmith.message.provider.MessageProvider
import it.fulminazzo.blocksmith.message.receiver.PlayerReceiverFactory
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

@Slf4j
class MessengerTest extends Specification {

    private Player player
    private MessageProvider provider
    private Messenger messenger

    private Map<String, Component> messages = [:]

    void setup() {
        ReceiverFactories.factories.add(new PlayerReceiverFactory())

        player = new Player("Luke")

        provider = Mock(MessageProvider)
        provider.getMessage(_, _) >> { a ->
            def message = messages[a[0]]
            if (message == null) throw new MessageNotFoundException(a[0], a[1])
            return message
        }

        messages['prefix'] = Component.text('blocksmith | ')

        messenger = new Messenger(log).setMessageProvider(provider)
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
        messenger.sendTitle(player, 'message', 'message')

        and:
        def lastTitle = player.lastTitle

        then:
        lastTitle[TitlePart.TITLE] == expected

        and:
        lastTitle[TitlePart.SUBTITLE] == expected

        and:
        lastTitle[TitlePart.TIMES] == Title.Times.times(
                Duration.of(1L, ChronoUnit.SECONDS),
                Duration.of(2L, ChronoUnit.SECONDS),
                Duration.of(1L, ChronoUnit.SECONDS)
        )
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
