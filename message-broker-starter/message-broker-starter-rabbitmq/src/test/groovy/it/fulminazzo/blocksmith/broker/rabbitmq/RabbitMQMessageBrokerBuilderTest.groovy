package it.fulminazzo.blocksmith.broker.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.util.concurrent.ExecutorService

class RabbitMQMessageBrokerBuilderTest extends Specification {

    def 'test that IOException on newConnection is re-thrown as RabbitMQMessageBrokerException'() {
        given:
        def connectionFactory = Mock(ConnectionFactory)
        connectionFactory.newConnection(_ as ExecutorService) >> { throw new IOException('Test exception') }
        connectionFactory.host >> '0.0.0.0'
        connectionFactory.port >> 1234

        and:
        def builder = new RabbitMQMessageBrokerBuilder().executor(Mock(ExecutorService))
        Reflect.on(builder).set('connectionFactory', connectionFactory)

        when:
        builder.build()

        then:
        def e = thrown(RabbitMQMessageBrokerException)
        e.message =~ /.*0\.0\.0\.0:1234.+Test exception.*/
    }

}
