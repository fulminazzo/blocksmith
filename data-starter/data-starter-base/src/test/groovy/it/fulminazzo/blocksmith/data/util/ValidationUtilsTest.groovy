package it.fulminazzo.blocksmith.data.util

import spock.lang.Specification

class ValidationUtilsTest extends Specification {

    def 'test that checkNatural throws for #value'() {
        when:
        ValidationUtils.checkNatural(value, 'value')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid value $value. Must be at least 1"

        where:
        value << [
                Integer.MIN_VALUE, -1, 0
        ]
    }

    def 'test that checkPositive throws for #value'() {
        when:
        ValidationUtils.checkPositive(value, 'value')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid value $value. Must be at least 0"

        where:
        value << [
                Integer.MIN_VALUE, -1
        ]
    }

    def 'test that checkGreaterEqualThan throws for #value'() {
        when:
        ValidationUtils.checkGreaterEqualThan(value, 'value', 2)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid value $value. Must be at least 2"

        where:
        value << [
                Integer.MIN_VALUE, 0, 1
        ]
    }

    def 'test that checkPort throws for #port'() {
        when:
        ValidationUtils.checkPort(port)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid port $port. Must be between ${ValidationUtils.MIN_PORT} and ${ValidationUtils.MAX_PORT}"

        where:
        port << [
                -10, ValidationUtils.MIN_PORT - 1,
                ValidationUtils.MAX_PORT + 1, Integer.MAX_VALUE
        ]
    }

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
