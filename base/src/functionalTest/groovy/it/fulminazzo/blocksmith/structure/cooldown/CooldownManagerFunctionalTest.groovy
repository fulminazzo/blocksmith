package it.fulminazzo.blocksmith.structure.cooldown

import spock.lang.Specification

import java.time.Duration

class CooldownManagerFunctionalTest extends Specification {

    def 'test that CooldownManager works'() {
        given:
        def manager = new CooldownManager()
        def entity = new Object()

        expect:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity, Duration.ofSeconds(-1))

        then:
        thrown(IllegalArgumentException)

        and:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity, Duration.ofSeconds(1))

        then:
        manager.isOnCooldown(entity)

        when:
        def remaining = manager.getRemaining(entity)

        then:
        remaining <= 1_000
        remaining >= 0

        when:
        sleep(1_000)

        then:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity, 1_000).remove(entity)

        then:
        !manager.isOnCooldown(entity)

        when:
        manager.getRemaining(entity)

        then:
        thrown(IllegalArgumentException)
    }

}
