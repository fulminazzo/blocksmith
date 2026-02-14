package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class ValidationUtilsTest extends Specification {

    def 'test that validate does not throw on valid bean'() {
        given:
        def person = new Person('Alex', 'Fulminazzo', 18)

        when:
        ValidationUtils.validate(person)

        then:
        noExceptionThrown()
    }

    def 'test that validate fails verification for #person'() {
        when:
        ValidationUtils.validate(person)

        then:
        def e = thrown(ValidationUtils.ViolationException)
        e.message == expected

        where:
        person                               || expected
        new Person(null, 'Fulminazzo', 18)   || 'Name cannot be empty'
        new Person('', 'Fulminazzo', 18)     || 'Name cannot be empty'
        new Person('Alex', null, 18)         || 'Lastname cannot be empty'
        new Person('Alex', '', 18)           || 'Lastname cannot be empty'
        new Person('Alex', 'Fulminazzo', -1) || 'Must be at least 18 years old to use the application'
        new Person('Alex', 'Fulminazzo', 0)  || 'Must be at least 18 years old to use the application'
    }

    def 'test that validateField does not throw on valid #fieldName'() {
        when:
        ValidationUtils.validateField(Person, fieldName, fieldValue)

        then:
        noExceptionThrown()

        where:
        fieldName  | fieldValue
        'name'     | 'Alex'
        'lastname' | 'Fulminazzo'
        'age'      | 18
    }

    def 'test that validateField fails verification for #fieldName'() {
        when:
        ValidationUtils.validateField(Person, fieldName, fieldValue)

        then:
        def e = thrown(ValidationUtils.ViolationException)
        e.message == expected

        where:
        fieldName  | fieldValue || expected
        'name'     | null       || 'Name cannot be empty'
        'name'     | ''         || 'Name cannot be empty'
        'lastname' | null       || 'Lastname cannot be empty'
        'lastname' | ''         || 'Lastname cannot be empty'
        'age'      | -1         || 'Must be at least 18 years old to use the application'
        'age'      | 0          || 'Must be at least 18 years old to use the application'
    }

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
