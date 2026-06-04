package it.fulminazzo.blocksmith.broker.tcp.peer

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

@Slf4j
@Stepwise
class TcpMessagePeerIntegrationTest extends Specification {
    private static final long AWAIT_TIME = 5_000L
    private static final long RETRY_INTERVAL = 1_000L

    private static int port = 30015

    private final Executor executor = Executors.newCachedThreadPool()

    private final Mapper mapper = MapperFormat.JSON.newMapper()

    private TcpMessagePeer peer1
    private TcpMessagePeer peer2

    void setup() {
        peer1 = new TcpMessagePeer(
                log,
                mapper,
                port,
                RETRY_INTERVAL,
                executor
        )
        peer2 = new TcpMessagePeer(
                log,
                mapper,
                port,
                RETRY_INTERVAL,
                executor
        )
    }

    void cleanup() {
        peer1?.close()
        peer2?.close()
        executor.shutdown()
    }

    def 'test peers lifecycle'() {
        when: 'first peer starts'
        peer1.start()

        and:
        sleep(AWAIT_TIME)

        then: 'server and client should be started'
        peer1.server().filter { !it.closed }.present
        peer1.client().filter { !it.closed }.present

        when: 'second peer starts'
        peer2.start()

        and:
        sleep(AWAIT_TIME)

        then: 'only client should be started'
        peer2.server().empty
        peer2.client().filter { !it.closed }.present

        when: 'first peer dies'
        peer1.close()

        and:
        sleep(AWAIT_TIME + RETRY_INTERVAL)

        then: 'second peer should re-start with both server and client'
        peer2.server().filter { !it.closed }.present
        peer2.client().filter { !it.closed }.present
    }

    def 'test that handleMessage delegates to handlers'() {
        given:
        peer1.start()

        and:
        def message = new AtomicReference<String>()
        def handler = m -> message.set(m)
        peer1.registerHandler('test', handler)

        when:
        peer1.client().ifPresent { it.handleMessage('test', 'Hello, world!') }

        then:
        message.get() == 'Hello, world!'

        when:
        message.set(null)
        peer1.client().ifPresent { it.handleMessage('main', 'Hello, world!') }

        then:
        message.get() == null

        when:
        peer1.unregisterHandler(handler)
        peer1.client().ifPresent { it.handleMessage('test', 'Hello, world!') }

        then:
        message.get() == null
    }

}
