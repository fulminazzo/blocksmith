package it.fulminazzo.blocksmith.structure.expiring

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringMapTest extends ExpiringMapImplTest {
    private static ScheduledExecutorService scheduler

    void setupSpec() {
        scheduler = Executors.newSingleThreadScheduledExecutor()
    }

    void cleanupSpec() {
        scheduler.close()
    }

    @Override
    protected ExpiringMap<String, String> createMap() {
        return new ScheduledExpiringMap<>(scheduler, Duration.ofMillis(1L))
    }

}
