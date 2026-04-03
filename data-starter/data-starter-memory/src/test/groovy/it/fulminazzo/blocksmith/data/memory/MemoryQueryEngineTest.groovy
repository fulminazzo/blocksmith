package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap
import spock.lang.Specification

import java.util.concurrent.Executors

class MemoryQueryEngineTest extends Specification {

    def 'test query method'() {
        given:
        def executor = Executors.newSingleThreadExecutor()
        def engine = new MemoryQueryEngine<String, String>(ExpiringMap.lazy(), executor)

        when:
        engine.query(m -> m.put('hello', 'world', 3600_000)).get()

        and:
        def returned = engine.query(m -> m.get('hello')).get()

        then:
        returned == 'world'

        cleanup:
        executor?.shutdown()
    }

}
