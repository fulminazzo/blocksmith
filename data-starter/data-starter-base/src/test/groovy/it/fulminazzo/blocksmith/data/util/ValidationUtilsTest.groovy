package it.fulminazzo.blocksmith.data.util

import spock.lang.Specification

class ValidationUtilsTest extends Specification {

    def 'test that checkInRange throws for #value'() {
        when:
        ValidationUtils.checkInRange(value, 'value', 1, 5)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid value $value. Must be between 1 and 5"

        where:
        value << [
                0, -1,
                6, 7
        ]
    }

}
