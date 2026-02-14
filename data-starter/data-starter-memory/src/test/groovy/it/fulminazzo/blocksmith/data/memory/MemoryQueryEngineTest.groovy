package it.fulminazzo.blocksmith.data.memory

import spock.lang.Specification

import java.util.concurrent.Executors

class MemoryQueryEngineTest extends Specification {

    def 'test query method'() {
        given:
        def executor = Executors.newSingleThreadExecutor()
        def engine = new MemoryQueryEngine<String, String>(executor)

        when:
        engine.query(m -> m.put('hello', 'world')).get()

        and:
        def returned = engine.query(m -> m.get('hello')).get()

        then:
        returned == 'world'

        cleanup:
        executor?.shutdown()
    }

}
