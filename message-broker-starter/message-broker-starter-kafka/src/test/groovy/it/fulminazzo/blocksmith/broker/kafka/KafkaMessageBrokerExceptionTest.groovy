package it.fulminazzo.blocksmith.broker.kafka

import spock.lang.Specification

class KafkaMessageBrokerExceptionTest extends Specification {

    def 'test that getCauseMessage of #throwable returns #expected'() {
        when:
        def actual = KafkaMessageBrokerException.getCauseMessage(throwable)

        then:
        actual == expected

        where:
        throwable                                      || expected
        new RuntimeException()                         || RuntimeException.simpleName
        new RuntimeException('Test runtime exception') || 'Test runtime exception'
    }

    def 'test that unwrapCause of #throwable returns #expected'() {
        when:
        def actual = KafkaMessageBrokerException.unwrapCause(throwable)

        then:
        expected.class == actual.class
        expected.message == actual.message

        where:
        throwable                                      || expected
        new RuntimeException()                         || new RuntimeException()
        new RuntimeException('')                       || new RuntimeException('')
        new RuntimeException('Test runtime exception') || new RuntimeException('Test runtime exception')
        new RuntimeException(
                null,
                new Exception()
        )                                              || new Exception()
        new RuntimeException(
                null,
                new Exception('Test exception')
        )                                              || new Exception('Test exception')
        new RuntimeException(
                '',
                new Exception()
        )                                              || new Exception()
        new RuntimeException(
                '',
                new Exception('Test exception')
        )                                              || new Exception('Test exception')
        new RuntimeException(
                'Test runtime exception',
                new Exception('Test exception')
        )                                              || new RuntimeException(
                'Test runtime exception',
                new Exception('Test exception')
        )
    }

}
