package it.fulminazzo.blocksmith.broker.tcp.peer.client

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.tcp.peer.MessageDto
import it.fulminazzo.blocksmith.broker.tcp.peer.server.ServerResponse
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

@Slf4j
class TcpMessageClientTest extends Specification {
    private static final long AWAIT_TIME = 10_000L
    private static final long TEST_WAIT_TIME = 500
    private static final String CHANNEL_NAME = 'channel'

    private static int port = 30005

    private final Mapper mapper = MapperFormat.JSON.newMapper()

    private MockTcpMessageServer server = new MockTcpMessageServer(port++)

    void setup() {
        Thread.startDaemon { server.run() }
    }

    def 'test that handleMessage correctly handles all kinds of messages'() {
        given:
        def received = [:]

        when:
        def client = new TcpMessageClient(log, mapper, server.port) {
            @Override
            void handleMessage(final @NotNull String channel, final @NotNull String message) {
                received[channel] = message
            }
        }
        Thread.startDaemon { client.run() }
        sleep(TEST_WAIT_TIME)

        then:
        noExceptionThrown()

        when:
        server.send(ServerResponse.SUCCESS)
        sleep(TEST_WAIT_TIME)
        
        then:
        noExceptionThrown()
        
        and:
        received.size() == 0
        
        when:
        server.send(ServerResponse.UNKNOWN_COMMAND)
        sleep(TEST_WAIT_TIME)

        then:
        noExceptionThrown()

        and:
        received.size() == 0
        
        when:
        server.send(mapper.serialize(new MessageDto('Hello', 'world')))
        sleep(TEST_WAIT_TIME)
        
        then:
        received['Hello'] == 'world'
    }

    def 'test that subscribe and unsubscribe send correct commands'() {
        when:
        def client = new TcpMessageClient(log, mapper, server.port) {
            @Override
            void handleMessage(final @NotNull String channel, final @NotNull String message) {

            }
        }
        Thread.startDaemon { client.run() }
        sleep(TEST_WAIT_TIME)

        then:
        noExceptionThrown()

        when:
        client.subscribe(CHANNEL_NAME)

        then:
        server.received("SUBSCRIBE $CHANNEL_NAME")

        and:
        client.isSubscribed(CHANNEL_NAME)

        when:
        client.unsubscribe(CHANNEL_NAME)

        then:
        server.received("UNSUBSCRIBE $CHANNEL_NAME")

        and:
        !client.isSubscribed(CHANNEL_NAME)

        cleanup:
        client?.close()
    }

    static final class MockTcpMessageServer implements Runnable, Closeable {
        private final List<String> received = []

        private final ServerSocket serverSocket

        private Socket clientSocket
        private Reader clientInput
        private Writer clientOutput

        MockTcpMessageServer(final int port) {
            serverSocket = new ServerSocket(port)
        }

        boolean received(final String message) {
            def start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start <= AWAIT_TIME) {
                if (received.remove(message)) return true
                sleep(TEST_WAIT_TIME)
            }
            return false
        }
        
        void send(final String message) {
            clientOutput.write("$message\n")
            clientOutput.flush()
        }

        int getPort() {
            return serverSocket.localPort
        }

        @Override
        void run() {
            clientSocket = serverSocket.accept()
            clientInput = new InputStreamReader(clientSocket.inputStream)
            clientOutput = new OutputStreamWriter(clientSocket.outputStream)

            String line
            while ((line = clientInput.readLine()) != null)
                received.add(line)
        }

        @Override
        void close() {
            try {
                clientOutput?.close()
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                clientInput?.close()
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                clientSocket?.close()
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                serverSocket.close()
            } catch (IOException ignored) {
                // do nothing
            }
        }

    }

}
