package it.fulminazzo.blocksmith.structure.expiring


import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringListTest extends ExpiringListImplTest {
    private static ScheduledExecutorService scheduler

    void setupSpec() {
        scheduler = Executors.newSingleThreadScheduledExecutor()
    }

    void cleanupSpec() {
        scheduler.close()
    }

    @Override
    protected ExpiringList<String> createList() {
        return new ScheduledExpiringList<>(scheduler, Duration.ofMillis(1L))
    }

    def 'test that initialization of invalid duration throws'() {
        when:
        new ScheduledExpiringList<>(scheduler, Duration.ofSeconds(0))

        then:
        thrown(IllegalArgumentException)
    }

}
