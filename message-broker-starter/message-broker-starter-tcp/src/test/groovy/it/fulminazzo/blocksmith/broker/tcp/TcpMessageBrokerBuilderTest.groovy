package it.fulminazzo.blocksmith.broker.tcp

import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.Executors

@Slf4j
class TcpMessageBrokerBuilderTest extends Specification {

    def 'test that build does not throw if no logger is provided'() {
        given:
        def executor = Executors.newSingleThreadExecutor()
        def builder = new TcpMessageBrokerBuilder().executor(executor)

        when:
        def broker = builder.build()

        then:
        broker != null

        cleanup:
        broker?.close()
        executor?.close()
    }

    def 'test that build does not throw if executor is not provided'() {
        given:
        def builder = new TcpMessageBrokerBuilder().logger(log)

        when:
        def broker = builder.build()

        then:
        broker != null

        cleanup:
        broker?.close()
    }

}
