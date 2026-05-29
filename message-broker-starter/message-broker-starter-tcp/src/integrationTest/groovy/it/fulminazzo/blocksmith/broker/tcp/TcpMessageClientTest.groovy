package it.fulminazzo.blocksmith.broker.tcp

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.tcp.server.ChannelDto
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

import java.util.concurrent.CopyOnWriteArrayList

@Slf4j
class TcpMessageClientTest extends Specification {
    private static final int SLEEP_TIME = 10_000

    private static final String CHANNEL_NAME = 'main'

    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()
    private static final String SERIALIZED_CHANNEL = MAPPER.serialize(new ChannelDto(CHANNEL_NAME))

    private static int PORT = 30005

    def 'test that client starts and receives valid response for correct connection'() {
        given:
        def server = new MockTcpMessageServer(PORT, 'OK')
        def serverThread = Thread.start { server.run() }

        when:
        def client = new TcpMessageClient(log, MAPPER, '0.0.0.0', PORT, CHANNEL_NAME)
        def clientThread = Thread.startDaemon { client.start() }

        then:
        !client.closed

        when:
        client.write('Hello, world!\n')

        then:
        server.received('Hello, world!')

        cleanup:
        client?.close()
        clientThread?.interrupt()
        server?.close()
        serverThread?.interrupt()
    }

    def 'test that client is closed if server response is #response'() {
        given:
        def server = new MockTcpMessageServer(++PORT, response)
        def serverThread = Thread.startDaemon { server.run() }

        when:
        def client = new TcpMessageClient(log, MAPPER, '0.0.0.0', PORT, CHANNEL_NAME)
        def clientThread = Thread.startDaemon { client.start() }

        and:
        sleep(SLEEP_TIME)

        then:
        client.closed

        cleanup:
        client?.close()
        clientThread?.interrupt()
        server?.close()
        serverThread?.interrupt()

        where:
        response << [null, 'INVALID']
    }

    static final class MockTcpMessageServer implements Runnable, Closeable {
        private final List<String> received = new CopyOnWriteArrayList<>()

        private final ServerSocket serverSocket

        private final String response

        private Socket clientSocket
        private BufferedReader clientInput
        private Writer clientOutput

        MockTcpMessageServer(final int port, final String response) {
            serverSocket = new ServerSocket(port)
            this.response = response
        }

        boolean received(final String message) {
            def end = System.currentTimeMillis() + SLEEP_TIME
            while (System.currentTimeMillis() <= end) {
                if (received.remove(message)) return true
                sleep(250)
            }
            return false
        }

        @Override
        void run() {
            try {
                clientSocket = serverSocket.accept()
                clientInput = clientSocket.inputStream.newReader()
                clientOutput = clientSocket.outputStream.newWriter()

                String read = clientInput.readLine()
                if (read != null) {
                    received.add(read)

                    if (read == SERIALIZED_CHANNEL) {
                        if (response != null) {
                            clientOutput.write("$response\n")
                            clientOutput.flush()
                        } else {
                            close()
                            return
                        }
                    }

                    read = clientInput.readLine()
                    if (read != null) received.add(read)
                }
            } catch (IOException ignored) {
                // do nothing
            }
        }

        @Override
        void close() {
            try {
                clientInput?.close()
            } catch (IOException ignored) {
                // do nothing
            }
            try {
                clientOutput?.close()
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
