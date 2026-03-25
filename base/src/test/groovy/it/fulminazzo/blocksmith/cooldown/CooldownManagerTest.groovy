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
        Thread.sleep(1000)

        then:
        !manager.isOnCooldown(target)
    }

}
