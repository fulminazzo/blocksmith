package it.fulminazzo.blocksmith

import spock.lang.Specification

class BlocksmithTest extends Specification {

    def 'test that testing environment works'() {
        expect:
        true
    }

    def 'test that tests are running on provided JDK version'() {
        when:
        def version = Runtime.version().feature()

        then:
        version != 11
    }

}
