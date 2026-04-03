package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class ResourceUtilsTest extends Specification {

    def 'test that getResource with #arguments does not throw'() {
        when:
        def resource = ResourceUtils.getResource(*arguments)

        then:
        resource != null

        and:
        resource.readLines() == ['Hello, world!']

        where:
        arguments << [
                ['test.txt'],
                [ResourceUtils.classLoader, 'test.txt']
        ]
    }

}
