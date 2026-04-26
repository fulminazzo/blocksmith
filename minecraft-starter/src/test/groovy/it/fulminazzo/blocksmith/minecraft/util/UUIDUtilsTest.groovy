package it.fulminazzo.blocksmith.minecraft.util

import spock.lang.Specification

class UUIDUtilsTest extends Specification {

    def 'test that dashed and undashed work'() {
        given:
        def uuid = UUID.randomUUID()

        when:
        def undashed = UUIDUtils.undashed(uuid)

        and:
        def dashed = UUIDUtils.dashed(undashed)

        then:
        dashed == uuid
    }

}
