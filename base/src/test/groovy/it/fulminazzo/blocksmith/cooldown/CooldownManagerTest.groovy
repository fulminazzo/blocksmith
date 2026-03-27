package it.fulminazzo.blocksmith.cooldown

import spock.lang.Specification

import java.time.Duration

class CooldownManagerTest extends Specification {

    def 'test that cooldown system works'() {
        given:
        def manager = new CooldownManager(Duration.ofSeconds(1))

        and:
        def target = new Object()

        expect:
        !manager.isOnCooldown(target)

        when:
        manager.putOnCooldown(target)

        then:
        noExceptionThrown()

        and:
        manager.isOnCooldown(target)

        when:
        def remaining = manager.getRemainingCooldown(target)

        then:
        remaining < 1000

        when:
        Thread.sleep(1000)

        then:
        !manager.isOnCooldown(target)

        when:
        manager.putOnCooldown(target)
        manager.removeFromCooldown(target)

        then:
        !manager.isOnCooldown(target)

        when:
        manager.getRemainingCooldown(target)

        then:
        thrown(IllegalArgumentException)
    }

}
