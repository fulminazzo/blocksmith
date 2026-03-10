package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class ResourceUtilsTest extends Specification {

    def 'test that listResources returns all the requested resources'() {
        given:
        def expected = ['users.json', 'passwords.txt', 'dump.sql']

        when:
        def actual = ResourceUtils.listResources('data')

        then:
        actual.sort() == expected.sort()
    }

}
