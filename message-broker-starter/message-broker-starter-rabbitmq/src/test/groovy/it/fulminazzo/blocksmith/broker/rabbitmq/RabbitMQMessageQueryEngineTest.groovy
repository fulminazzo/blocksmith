package it.fulminazzo.blocksmith.broker.rabbitmq

import com.rabbitmq.client.Channel
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RabbitMQMessageQueryEngineTest extends Specification {
    private static final String EXCHANGE_NAME = 'channel'
    private static final String ROUTING_KEY = 'routing-key'
    private static final String QUEUE_NAME = 'queue'

    private final ExecutorService executor = Executors.newSingleThreadExecutor()
    private final Channel channel = Mock(Channel)

    private RabbitMQMessageQueryEngine queryEngine

    void setup() {
        queryEngine = Spy(RabbitMQMessageQueryEngine, constructorArgs : [
                executor,
                EXCHANGE_NAME,
                channel,
                ROUTING_KEY,
                new RabbitMQMessageChannelSettings.QueueSettings()
                        .withQueueName(QUEUE_NAME)
        ])
        queryEngine.channelName >> EXCHANGE_NAME
    }

    void cleanup() {
        executor.shutdown()
    }

    def 'test that IOException on publish is re-thrown as RabbitMQMessageBrokerException'() {
        given:
        channel.basicPublish(_, _, _, _) >> { throw new IOException('Test exception') }

        and:
        final String payload = 'Hello, world!'

        when:
        queryEngine.publish(payload).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def cause = e.cause
        RabbitMQMessageBrokerException.isInstance(cause)
        cause.message =~ /.*$payload.+Test exception.*/
    }

    def 'test that IOException on queue bind is re-thrown as RabbitMQMessageBrokerException'() {
        given:
        channel.queueDeclare(_, _, _, _, _) >> { throw new IOException('Test exception') }

        when:
        queryEngine.listen(i -> { })

        then:
        def e = thrown(RabbitMQMessageBrokerException)
        e.message =~ /.*$QUEUE_NAME.+$EXCHANGE_NAME.+$ROUTING_KEY.+Test exception.*/
    }

    def 'test that IOException on consumer register is re-thrown as RabbitMQMessageBrokerException'() {
        given:
        channel.basicConsume(_, _, _, _) >> { throw new IOException('Test exception') }

        when:
        queryEngine.listen(i -> { })

        then:
        def e = thrown(RabbitMQMessageBrokerException)
        e.message =~ /.*$QUEUE_NAME.+Test exception.*/
    }

}
