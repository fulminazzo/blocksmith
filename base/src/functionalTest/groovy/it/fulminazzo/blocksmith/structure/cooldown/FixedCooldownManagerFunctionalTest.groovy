package it.fulminazzo.blocksmith.structure.cooldown

import spock.lang.Specification

import java.time.Duration

class FixedCooldownManagerFunctionalTest extends Specification {

    def 'test that FixedCooldownManager works'() {
        given:
        def manager = new FixedCooldownManager(Duration.ofSeconds(1))
        def entity = new Object()

        expect:
        !manager.isOnCooldown(entity)

        and:
        !manager.isOnCooldown(entity)

        when:
        manager.put(entity)

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
        manager.put(entity).remove(entity)

        then:
        !manager.isOnCooldown(entity)

        when:
        manager.getRemaining(entity)

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that initializing FixedCooldownManager with invalid duration throws'() {
        when:
        new FixedCooldownManager(Duration.ofSeconds(-1))

        then:
        thrown(IllegalArgumentException)
    }

}
