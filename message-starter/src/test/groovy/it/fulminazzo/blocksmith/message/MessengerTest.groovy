package it.fulminazzo.blocksmith.message

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException
import it.fulminazzo.blocksmith.message.provider.MessageProvider
import it.fulminazzo.blocksmith.message.receiver.PlayerReceiverFactory
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories
import net.kyori.adventure.text.Component
import spock.lang.Specification

@Slf4j
class MessengerTest extends Specification {

    private Player player
    private MessageProvider provider
    private Messenger messenger

    void setup() {
        ReceiverFactories.factories.add(new PlayerReceiverFactory())

        player = new Player()

        provider = Mock(MessageProvider)

        messenger = new Messenger(log).setMessageProvider(provider)
    }

    def 'test that sendMessage correctly converts and sends message'() {
        given:
        def expected = Component.text('Hello, world!')

        and:
        provider.getMessage('message', Locale.ITALY) >> {
            expected
        }

        and:
        player.locale = Locale.ITALY

        when:
        messenger.sendMessage(player, 'message')

        then:
        def lastMessage = player.lastMessage
        lastMessage == expected
    }

    def 'test that sendMessage does not throw on not found'() {
        given:
        provider.getMessage(_, _) >> {
            throw new MessageNotFoundException('message', Locale.default)
        }

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
        1 * provider.getMessage(code, locale)
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
