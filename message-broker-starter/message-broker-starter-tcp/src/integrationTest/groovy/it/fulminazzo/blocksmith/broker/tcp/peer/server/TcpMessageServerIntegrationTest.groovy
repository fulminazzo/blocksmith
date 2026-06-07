package it.fulminazzo.blocksmith.broker.tcp.peer.server

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.tcp.peer.MessageDto
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

import java.util.concurrent.Executors

@Slf4j
class TcpMessageServerIntegrationTest extends Specification {
    private static final long AWAIT_TIME = 2_000L
    private static final long TEST_WAIT_TIME = 500

    private static int port = 30015

    private final Mapper mapper = MapperFormat.JSON.newMapper()

    private final TcpMessageServer server = new TcpMessageServer(log, mapper, port++, Executors.newCachedThreadPool())

    void setup() {
        Thread.startDaemon { server.run() }
    }

    void cleanup() {
        server.close()
    }

    def 'test that broadcast only broadcasts to clients subscribed to the channel'() {
        given:
        def client1 = new MockTcpMessageClient(server.port).start()
        def client2 = new MockTcpMessageClient(server.port).start()
        def client3 = new MockTcpMessageClient(server.port).start()

        and:
        client1.send(ServerCommand.SUBSCRIBE.formatCommand('client1'))
        client1.send(ServerCommand.SUBSCRIBE.formatCommand('client12'))
        client1.send(ServerCommand.SUBSCRIBE.formatCommand('client13'))

        and:
        client2.send(ServerCommand.SUBSCRIBE.formatCommand('client2'))
        client2.send(ServerCommand.SUBSCRIBE.formatCommand('client12'))
        client2.send(ServerCommand.SUBSCRIBE.formatCommand('client23'))

        and:
        client3.send(ServerCommand.SUBSCRIBE.formatCommand('client3'))
        client3.send(ServerCommand.SUBSCRIBE.formatCommand('client13'))
        client3.send(ServerCommand.SUBSCRIBE.formatCommand('client23'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client1', 'message1'))

        then:
        client1.received(formatMessage('client1', 'message1'))
        !client2.received(formatMessage('client1', 'message1'))
        !client3.received(formatMessage('client1', 'message1'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client2', 'message2'))

        then:
        !client1.received(formatMessage('client2', 'message2'))
        client2.received(formatMessage('client2', 'message2'))
        !client3.received(formatMessage('client2', 'message2'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client3', 'message3'))

        then:
        !client1.received(formatMessage('client3', 'message3'))
        !client2.received(formatMessage('client3', 'message3'))
        client3.received(formatMessage('client3', 'message3'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client12', 'message12'))

        then:
        client1.received(formatMessage('client12', 'message12'))
        client2.received(formatMessage('client12', 'message12'))
        !client3.received(formatMessage('client12', 'message12'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client13', 'message13'))

        then:
        client1.received(formatMessage('client13', 'message13'))
        !client2.received(formatMessage('client13', 'message13'))
        client3.received(formatMessage('client13', 'message13'))

        when:
        client1.send(ServerCommand.MESSAGE.formatCommand('client23', 'message23'))

        then:
        !client1.received(formatMessage('client23', 'message23'))
        client2.received(formatMessage('client23', 'message23'))
        client3.received(formatMessage('client23', 'message23'))

        cleanup:
        client1?.close()
        client2?.close()
        client3?.close()
    }

    def 'test that getActiveClients does not return dead clients'() {
        given:
        def client1 = new MockTcpMessageClient(server.port).start()
        def client2 = new MockTcpMessageClient(server.port).start()
        def client3 = new MockTcpMessageClient(server.port).start()

        expect:
        server.activeClients.size() == 3

        when:
        client1.close()

        and:
        sleep(TEST_WAIT_TIME)

        then:
        server.activeClients.size() == 2

        when:
        client2.close()

        and:
        sleep(TEST_WAIT_TIME)

        then:
        server.activeClients.size() == 1

        when:
        client3.close()
        sleep(TEST_WAIT_TIME)

        then:
        server.activeClients.empty

        cleanup:
        client1?.close()
        client2?.close()
        client3?.close()
    }

    protected String formatMessage(final String channel, final String message) {
        return mapper.serialize(new MessageDto(channel, message))
    }

    private static class MockTcpMessageClient implements Runnable, Closeable {
        private final List<String> received = []

        private final Socket clientSocket
        private final Reader clientInput
        private final Writer clientOutput

        MockTcpMessageClient(final int port) {
            clientSocket = new Socket('localhost', port)
            clientInput = new InputStreamReader(clientSocket.inputStream)
            clientOutput = new OutputStreamWriter(clientSocket.outputStream)
        }

        MockTcpMessageClient start() {
            Thread.startDaemon { this.run() }
            return this
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

        @Override
        void run() {
            try {
                String line
                while ((line = clientInput.readLine()) != null)
                    received.add(line)
            } catch (IOException ignored) {
                // do nothing
            }
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
        }

    }

}
