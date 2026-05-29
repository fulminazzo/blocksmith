package it.fulminazzo.blocksmith.broker.tcp.server

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CopyOnWriteArrayList

@Slf4j
class TcpMessageServerIntegrationTest extends Specification {
    private static final int SLEEP_TIME = 10_000

    private static final int PORT = 29005
    private static final String CHANNEL_NAME = 'main'

    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()
    private static final String SERIALIZED_CHANNEL = MAPPER.serialize(new ChannelDto(CHANNEL_NAME))

    @Shared
    private final TcpMessageServer server = new TcpMessageServer(log, MAPPER, PORT)

    void setupSpec() {
        Thread.startDaemon { server.run() }
    }

    void cleanupSpec() {
        server.close()
    }
    
    def 'test that server removes dead handlers'() {
        given:
        final def message = 'Hello, world'

        and:
        def first = new MockTcpMessageClient(PORT)
        def second = new MockTcpMessageClient(PORT)

        when:
        first.start()
        first.write(SERIALIZED_CHANNEL)

        then:
        first.received('OK')

        when:
        second.start()
        second.write(SERIALIZED_CHANNEL)

        then:
        second.received('OK')

        when:
        second.close()

        and:
        first.write(message)

        then:
        first.received(message)

        and:
        def handlers = server.clients[CHANNEL_NAME]
        handlers != null
        handlers.size() == 1

        cleanup:
        first?.close()
        second?.close()
    }

    def 'test that server correctly handles two clients and broadcasts messages'() {
        given:
        final def message = 'Hello, world'

        and:
        def first = new MockTcpMessageClient(PORT)
        def second = new MockTcpMessageClient(PORT)

        when: 'first client connects'
        first.start()

        and: 'sends channel name'
        first.write(SERIALIZED_CHANNEL)

        then: 'it should have received feedback from server'
        first.received('OK')

        when: 'second client connects'
        second.start()

        and: 'sends channel name'
        second.write(SERIALIZED_CHANNEL)

        then: 'it should have received feedback from server'
        second.received('OK')

        when: 'first client sends a message'
        first.write(message)

        then: 'they both should have received it'
        first.received(message)
        second.received(message)

        when: 'second client sends a message'
        second.write(message)

        then: 'they both should have received it'
        first.received(message)
        second.received(message)

        cleanup:
        first?.close()
        second?.close()
    }

    def 'test that server correctly handles one client and broadcasts messages'() {
        given:
        final def message = 'Hello, world'

        and:
        def client = new MockTcpMessageClient(PORT)

        when: 'client connects'
        client.start()

        and: 'sends channel name'
        client.write(SERIALIZED_CHANNEL)

        then: 'it should have received feedback from server'
        client.received('OK')

        when: 'client sends a message'
        client.write(message)

        then: 'it should have received it'
        client.received(message)

        cleanup:
        client?.close()
    }

    def 'test that server responds with error if channel name has not been specified correctly'() {
        given:
        def client = new MockTcpMessageClient(PORT)

        when:
        client.start()

        and:
        client.write(CHANNEL_NAME)

        then:
        client.received('Invalid connection. Please provide a channel name before sending any message.')
    }

    static final class MockTcpMessageClient implements Runnable, Closeable {
        private final List<String> received = new CopyOnWriteArrayList<>()

        private final Socket socket
        private final BufferedReader input
        private final Writer output

        MockTcpMessageClient(final int port) {
            socket = new Socket('0.0.0.0', port)
            input = socket.inputStream.newReader()
            output = socket.outputStream.newWriter()
        }

        boolean received(final String message) {
            def end = System.currentTimeMillis() + SLEEP_TIME
            while (System.currentTimeMillis() <= end) {
                if (received.remove(message)) return true
                sleep(250)
            }
            return false
        }

        void start() {
            Thread.startDaemon { this.run() }
        }

        void write(final String message) {
            output.write("$message\n")
            output.flush()
        }

        @Override
        void run() {
            try {
                String read
                while ((read = input.readLine()) != null) received.add(read)
            } catch (IOException ignored) {
                // do nothing
            }
        }

        @Override
        void close() {
            try {
                Thread.startDaemon { input.close() }
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                output.close()
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                socket.close()
            } catch (IOException ignored) {
                // do nothing
            }
        }

    }

}
