package it.fulminazzo.blocksmith.broker.rabbitmq

import com.rabbitmq.client.Connection
import it.fulminazzo.blocksmith.data.mapper.Mapper
import spock.lang.Specification

import java.util.concurrent.ExecutorService

class RabbitMQMessageBrokerTest extends Specification {

    def 'test that IOException on newChannel is re-thrown as RabbitMQMessageBrokerException'() {
        given:
        def connection = Mock(Connection)
        def broker = Spy(RabbitMQMessageBroker, constructorArgs : [
                Mock(ExecutorService),
                connection,
                Mock(Mapper)
        ])

        and:
        final exchangeName = 'exchange'

        and:
        connection.createChannel() >> { throw new IOException('Test exception') }

        when:
        broker.newChannel(new RabbitMQMessageChannelSettings().withChannelName(exchangeName))

        then:
        def e = thrown(RabbitMQMessageBrokerException)
        e.message =~ /.*$exchangeName.+Test exception.*/
    }

}
