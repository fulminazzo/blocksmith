package it.fulminazzo.blocksmith.scheduler

import spock.lang.Specification

import java.time.Duration

class TaskBuilderTest extends Specification {

    def 'test that task builder throws for #method'() {
        given:
        def builder = Mock(TaskBuilder)
        builder."$method"(_ as Duration) >> {
            callRealMethod()
        }

        when:
        builder."$method"(Duration.ofSeconds(-1))

        then:
        thrown(IllegalArgumentException)

        where:
        method << ['delay', 'interval']
    }

}
