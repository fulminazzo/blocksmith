package it.fulminazzo.blocksmith.structure.expiring

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringListTest extends ExpiringListImplTest {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor()

    void cleanupSpec() {
        SCHEDULER.close()
    }

    def 'test that initialization of invalid duration throws'() {
        when:
        new ScheduledExpiringList<>(SCHEDULER, Duration.ofSeconds(0))

        then:
        thrown(IllegalArgumentException)
    }

    @Override
    protected ExpiringList<String> createList() {
        return new ScheduledExpiringList<>(SCHEDULER, Duration.ofMillis(1L))
    }

}
