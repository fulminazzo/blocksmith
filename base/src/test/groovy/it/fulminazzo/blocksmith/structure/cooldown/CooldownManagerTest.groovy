package it.fulminazzo.blocksmith.structure.cooldown

import spock.lang.Specification

import java.time.Duration

class CooldownManagerTest extends Specification {

    def 'test that CooldownManager works'() {
        given:
        def manager = new CooldownManager()

        def entity = new Object()

        expect:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity, Duration.ofSeconds(-10))

        then:
        thrown(IllegalArgumentException)

        and:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity, Duration.ofSeconds(10))

        then:
        manager.isOnCooldown(entity)

        and:
        def remaining = manager.getRemaining(entity)

        then:
        remaining <= 10_000
        remaining >= 0

        when:
        manager.remove(entity)

        then:
        !manager.isOnCooldown(entity)

        when:
        manager.getRemaining(entity)

        then:
        thrown(IllegalArgumentException)
    }

}
