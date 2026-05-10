package it.fulminazzo.blocksmith.structure.expiring

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringMapTest extends ExpiringMapImplTest {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor()

    void cleanupSpec() {
        SCHEDULER.close()
    }

    @Override
    protected ExpiringMap<String, String> createMap() {
        return new ScheduledExpiringMap<>(SCHEDULER, Duration.ofMillis(1L))
    }

}
