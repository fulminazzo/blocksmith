package it.fulminazzo.blocksmith.broker.kafka

import it.fulminazzo.blocksmith.reflect.Reflect
import org.apache.kafka.clients.producer.KafkaProducer
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class KafkaMessageQueryEngineTest extends Specification {
    private static final String TOPIC_NAME = 'channel'
    private static final String KEY = 'key'

    private final ExecutorService executor = Executors.newSingleThreadExecutor()
    private final KafkaProducer<String, String> producer = Mock(KafkaProducer)

    private KafkaMessageQueryEngine queryEngine

    void setup() {
        queryEngine = Spy(KafkaMessageQueryEngine, constructorArgs : [
                executor,
                new Properties([
                        'bootstrap.servers' : '0.0.0.0:9002'
                ]),
                TOPIC_NAME,
                KEY,
                60_000L,
                125L
        ])
        def reflect = Reflect.on(queryEngine)
        reflect.get('producer').invoke('close')
        reflect.set('producer', producer)
    }

    void cleanup() {
        executor.shutdown()
    }

    def 'test that InterruptedException on publish is re-thrown as KafkaMessageBrokerException'() {
        given:
        producer.send(_) >> {
            def future = Mock(Future)
            future.get() >> { throw new InterruptedException('Test exception') }
            return future
        }

        and:
        final String payload = 'Hello, world!'

        when:
        queryEngine.publish(payload).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def cause = e.cause
        KafkaMessageBrokerException.isInstance(cause)
        cause.message =~ /.*$payload.+Test exception.*/
    }

    def 'test that ExecutionException on publish is re-thrown as KafkaMessageBrokerException'() {
        given:
        producer.send(_) >> executor.submit { throw new IllegalStateException('Test exception') }

        and:
        final String payload = 'Hello, world!'

        when:
        queryEngine.publish(payload).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def cause = e.cause
        KafkaMessageBrokerException.isInstance(cause)
        cause.message =~ /.*$payload.+Test exception.*/
    }

}
