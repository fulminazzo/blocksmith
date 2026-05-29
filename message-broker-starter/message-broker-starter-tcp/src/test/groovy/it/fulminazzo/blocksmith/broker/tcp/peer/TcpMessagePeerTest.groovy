package it.fulminazzo.blocksmith.broker.tcp.peer

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.tcp.peer.client.TcpMessageClient
import it.fulminazzo.blocksmith.data.mapper.Mapper
import spock.lang.Specification

import java.util.concurrent.ExecutorService

@Slf4j
class TcpMessagePeerTest extends Specification {
    private final TcpMessagePeer peer = Mock(TcpMessagePeer, constructorArgs : [
            log,
            Mock(Mapper),
            30035,
            1_000L,
            Mock(ExecutorService)
    ])

    def 'test subscribe unsubscribe cycle with client present'() {
        given:
        final channel = 'main'

        and:
        def client = Mock(TcpMessageClient)
        peer.client() >> Optional.of(client)
        peer.subscribe(_) >> { callRealMethod() }
        peer.unsubscribe(_) >> { callRealMethod() }

        when:
        peer.subscribe(channel)

        then:
        channel in peer.channels

        and:
        1 * client.subscribe(channel)

        when:
        peer.unsubscribe(channel)

        then:
        channel !in peer.channels

        and:
        1 * client.unsubscribe(channel)
    }

    def 'test subscribe unsubscribe cycle with client present'() {
        given:
        final channel = 'main'

        and:
        peer.client() >> Optional.empty()
        peer.subscribe(_) >> { callRealMethod() }
        peer.unsubscribe(_) >> { callRealMethod() }

        when:
        peer.subscribe(channel)

        then:
        channel in peer.channels

        when:
        peer.unsubscribe(channel)

        then:
        channel !in peer.channels
    }

}
