package it.fulminazzo.blocksmith.broker.tcp.peer.server

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

@Slf4j
class TcpMessageServerClientTest extends Specification {

    def 'test that handleMessage of MESSAGE ServerCommand broadcasts to server'() {
        given:
        def server = Mock(TcpMessageServer)

        and:
        def client = Mock(TcpMessageServerClient, constructorArgs : [
                server,
                log,
                MapperFormat.JSON.newMapper(),
                Mock(Socket) {
                    it.inputStream >> new ByteArrayInputStream([] as byte[])
                    it.outputStream >> new ByteArrayOutputStream()
                }
        ])
        client.handleMessage(_) >> { callRealMethod() }

        when:
        client.handleMessage("${ServerCommand.MESSAGE} Hello, world!")

        then:
        1 * server.broadcast('Hello, world!')
    }

    def 'test that handleMessage of #message sends #expected'() {
        given:
        def client = Mock(TcpMessageServerClient, constructorArgs : [
                Mock(TcpMessageServer),
                log,
                MapperFormat.JSON.newMapper(),
                Mock(Socket) {
                    it.inputStream >> new ByteArrayInputStream([] as byte[])
                    it.outputStream >> new ByteArrayOutputStream()
                }
        ])
        client.handleMessage(_) >> { callRealMethod() }
        client.subscribe(_) >> { client }
        client.unsubscribe(_) >> { client }

        when:
        client.handleMessage(message)

        then:
        1 * client.send(expected)

        where:
        message                             || expected
        'invalid'                           || ServerResponse.UNKNOWN_COMMAND
        'invalid command'                   || ServerResponse.UNKNOWN_COMMAND
        "${ServerCommand.SUBSCRIBE} test"   || ServerResponse.SUCCESS
        "${ServerCommand.UNSUBSCRIBE} test" || ServerResponse.SUCCESS
    }

}
