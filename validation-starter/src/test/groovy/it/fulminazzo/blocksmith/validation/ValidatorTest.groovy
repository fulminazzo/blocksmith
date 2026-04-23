package it.fulminazzo.blocksmith.validation

import it.fulminazzo.blocksmith.validation.annotation.Character
import it.fulminazzo.blocksmith.validation.annotation.Uuid
import spock.lang.Specification

import java.time.*

class ValidatorTest extends Specification {
    private static final Validator validator = Validator.instance

    private static final noValuesArray = new Object[0]
    private static final exceedValuesArray = (1..6).toArray()

    private static final char A = 'A'
    private static final char Z = 'Z'

    static {
        validator.register(Character, new ConstraintValidatorImpl((o) -> ((CharSequence) o).size() == 1, CharSequence))
    }

    def 'test that validate method works'() {
        given:
        def person = new Person('Alex', 23, new Person.School('Galileo'))

        when:
        person.setName('Steve', 'name update')

        then:
        noExceptionThrown()
    }

    def 'test that validate method throws for invalid parameter'() {
        given:
        def person = new Person('Alex', 23, null)

        when:
        person.setName(*parameters)

        then:
        def e = thrown(ViolationException)
        e.message == expected

        where:
        parameters               || expected
        [null, 'name update']    || 'invalid parameter at position 0: cannot be null'
        ['', 'name update']      || 'invalid parameter at position 0: \'\' is not allowed (only letters)'
        ['Alex!', 'name update'] || 'invalid parameter at position 0: \'Alex!\' is not allowed (only letters)'
        ['Alex', null]           || 'invalid parameter at position 1: cannot be null'
        ['Alex', '']             || 'invalid parameter at position 1: cannot be empty'
        [null, null]             || 'invalid parameter at position 0: cannot be null; ' +
                'invalid parameter at position 1: cannot be null'
        ['', null]               || 'invalid parameter at position 0: \'\' is not allowed (only letters); ' +
                'invalid parameter at position 1: cannot be null'
        [null, '']               || 'invalid parameter at position 0: cannot be null; ' +
                'invalid parameter at position 1: cannot be empty'
        ['', '']                 || 'invalid parameter at position 0: \'\' is not allowed (only letters); ' +
                'invalid parameter at position 1: cannot be empty'
    }

    def 'test that validate method throws if not enough parameters are given'() {
        given:
        def person = new Person('Alex', 23, null)

        when:
        person.invalidSetName('Alex', 'name updated')

        then:
        def e = thrown(IllegalArgumentException)
        e.message =~ '.*Please include all the parameters of the method to validate it.*'
    }

    def 'test that validate field works'() {
        given:
        def field = Person.getDeclaredField(fieldName)

        when:
        Validator.validateField(field, value)

        then:
        noExceptionThrown()

        where:
        fieldName | value
        'name'    | 'Alex'
        'age'     | 23
        'school'  | null
        'school'  | new Person.School('Galileo')
    }

    def 'test that validate field throws for invalid value'() {
        given:
        def field = Person.getDeclaredField('name')

        when:
        Validator.validateField(field, value)

        then:
        def e = thrown(ViolationException)
        e.message == expected

        where:
        value   || expected
        null    || 'invalid name: cannot be null'
        ''      || 'invalid name: \'\' is not allowed (only letters)'
        'Alex!' || 'invalid name: \'Alex!\' is not allowed (only letters)'
    }

    def 'test that validate of bean #bean works'() {
        when:
        Validator.validate(bean)

        then:
        noExceptionThrown()

        where:
        bean << [
                new Person('Alex', 23, new Person.School('Galileo')),
                null
        ]
    }

    def 'test that validate of #bean throws ViolationException with message #expected'() {
        when:
        Validator.validate(bean)

        then:
        def e = thrown(ViolationException)
        e.message == expected

        where:
        bean                                            || expected
        new Person(null, 23, null)                      || 'invalid property \'name\': cannot be null'
        new Person('', 23, null)                        || 'invalid property \'name\': \'\' is not allowed (only letters)'
        new Person('Alex!', 23, null)                   || 'invalid property \'name\': \'Alex!\' is not allowed (only letters)'
        new Person('Alex', 0, null)                     || 'invalid property \'age\': must be at least 18 and at most 115'
        new Person('Alex', 13, null)                    || 'invalid property \'age\': must be at least 18 and at most 115'
        new Person('Alex', 130, null)                   || 'invalid property \'age\': must be at least 18 and at most 115'
        new Person('Alex', 23, new Person.School(null)) || 'invalid property \'school.name\': cannot be null'
        new Person(null, 0, new Person.School(null))    ||
                'invalid property \'age\': must be at least 18 and at most 115; ' +
                'invalid property \'name\': cannot be null; ' +
                'invalid property \'school.name\': cannot be null'
    }

    def 'test that validate of field #fieldName and value #value does not throw'() {
        given:
        def field = Fields.getDeclaredField(fieldName)

        and:
        def toValidate = value
        if (fieldName.contains('OrNow') && value != null)
            toValidate = value()

        when:
        validator.validate(field, toValidate)

        then:
        noExceptionThrown()

        where:
        fieldName                | value
        // NonNull
        'nonNull'                | new Object()
        // AssertFalse
        'assertFalse'            | null
        'assertFalse'            | false
        // AssertTrue
        'assertTrue'             | null
        'assertTrue'             | true
        // Max
        'max'                    | null
        'max'                    | 0
        'max'                    | -1
        'max'                    | Integer.MIN_VALUE
        // Max Duration
        'maxDuration'            | null
        'maxDuration'            | Duration.ofMillis(0)
        'maxDuration'            | Duration.ofMillis(-1)
        'maxDuration'            | Duration.ofMillis(Integer.MIN_VALUE)
        // Max Character
        'maxCharacter'           | null
        'maxCharacter'           | Z
        // NegativeOrZero
        'negativeOrZero'         | null
        'negativeOrZero'         | 0
        'negativeOrZero'         | -1
        'negativeOrZero'         | Integer.MIN_VALUE
        // NegativeOrZero Duration
        'negativeOrZeroDuration' | null
        'negativeOrZeroDuration' | Duration.ofMillis(0)
        'negativeOrZeroDuration' | Duration.ofMillis(-1)
        'negativeOrZeroDuration' | Duration.ofMillis(Integer.MIN_VALUE)
        // Negative
        'negative'               | null
        'negative'               | -1
        'negative'               | Integer.MIN_VALUE
        // Negative Duration
        'negativeDuration'       | null
        'negativeDuration'       | Duration.ofMillis(-1)
        'negativeDuration'       | Duration.ofMillis(Integer.MIN_VALUE)
        // Min
        'min'                    | null
        'min'                    | 0
        'min'                    | 1
        'min'                    | Integer.MAX_VALUE
        // Min Duration
        'minDuration'            | null
        'minDuration'            | Duration.ofMillis(0)
        'minDuration'            | Duration.ofMillis(1)
        'minDuration'            | Duration.ofMillis(Integer.MAX_VALUE)
        // Min Character
        'minCharacter'           | null
        'minCharacter'           | A
        // PositiveOrZero
        'positiveOrZero'         | null
        'positiveOrZero'         | 0
        'positiveOrZero'         | 1
        'positiveOrZero'         | Integer.MAX_VALUE
        // PositiveOrZero Duration
        'positiveOrZeroDuration' | null
        'positiveOrZeroDuration' | Duration.ofMillis(0)
        'positiveOrZeroDuration' | Duration.ofMillis(1)
        'positiveOrZeroDuration' | Duration.ofMillis(Integer.MAX_VALUE)
        // Positive
        'positive'               | null
        'positive'               | 1
        'positive'               | Integer.MAX_VALUE
        // Positive Duration
        'positiveDuration'       | null
        'positiveDuration'       | Duration.ofMillis(1)
        'positiveDuration'       | Duration.ofMillis(Integer.MAX_VALUE)
        // Range
        'range'                  | null
        'range'                  | 1
        'range'                  | 10
        // Range Duration
        'rangeDuration'          | null
        'rangeDuration'          | Duration.ofMillis(1)
        'rangeDuration'          | Duration.ofMillis(10)
        // Range Character
        'rangeCharacter'         | null
        'rangeCharacter'         | A
        'rangeCharacter'         | 'F' as char
        'rangeCharacter'         | Z
        // Port
        'port'                   | null
        'port'                   | 1
        'port'                   | 65535
        // Size
        'sizeString'             | null
        'sizeString'             | 'a'
        'sizeString'             | 'a'.repeat(5)
        'sizeArray'              | null
        'sizeArray'              | ['a'].toArray()
        'sizeArray'              | (1..5).toArray()
        'sizeCollection'         | null
        'sizeCollection'         | ['a']
        'sizeCollection'         | (1..5).toList()
        'sizeMap'                | null
        'sizeMap'                | ['a']
        'sizeMap'                | (1..5).collectEntries { it -> [it, it] }
        // Matches
        'matches'                | null
        'matches'                | 'a'
        'matches'                | 'Alessandro'
        // Hostname
        'hostname'               | null
        'hostname'               | 'localhost'
        'hostname'               | 'example.com'
        'hostname'               | 'my-server.local'
        'hostname'               | 'sub.domain.co.uk'
        // Email
        'email'                  | null
        'email'                  | 'user@example.com'
        'email'                  | 'name.surname+tag@sub.domain.org'
        'email'                  | 'user123@my-domain.io'
        // IPV4
        'ipv4'                   | null
        'ipv4'                   | '127.0.0.1'
        'ipv4'                   | '0.0.0.0'
        'ipv4'                   | '255.255.255.255'
        'ipv4'                   | '192.168.1.100'
        // IPv6
        'ipv6'                   | null
        'ipv6'                   | '2001:0db8:85a3:0000:0000:8a2e:0370:7334'
        'ipv6'                   | '::1'
        'ipv6'                   | '::'
        'ipv6'                   | 'fe80::1'
        // Url
        'url'                    | null
        'url'                    | 'http://example.com'
        'url'                    | 'ftp://example.com'
        'url'                    | 'https://example.com'
        'url'                    | 'https://sub.domain.com/path?query=1#anchor'
        'url'                    | 'https://my-site.org:8080/api/v1'
        // HexColor
        'hexColor'               | null
        'hexColor'               | '#FF0000'
        'hexColor'               | '#abc123'
        'hexColor'               | '#000000'
        'hexColor'               | '#FFFFFFAA'
        'hexColor'               | '#FFFFFF'
        'hexColor'               | '#FFFA'
        'hexColor'               | '#FFF'
        // Identifier
        'identifier'             | null
        'identifier'             | 'myVariable'
        'identifier'             | '_private'
        'identifier'             | 'a1'
        'identifier'             | 'a'
        // Alphabetical
        'alphabetical'           | null
        'alphabetical'           | 'Hello'
        'alphabetical'           | 'world'
        'alphabetical'           | 'Alessandro'
        // AlphabeticalOrDigit
        'alphabeticalOrDigit'    | null
        'alphabeticalOrDigit'    | 'Hello123'
        'alphabeticalOrDigit'    | 'abc'
        'alphabeticalOrDigit'    | '12345'
        // NotBlank
        'notBlank'               | null
        'notBlank'               | 'hello'
        'notBlank'               | 'Hello, world!'
        'notBlank'               | 'a'
        // NotEmpty
        'notEmpty'               | null
        'notEmpty'               | 'hello'
        'notEmpty'               | ' '
        // Uuid
        'uuid'                   | UUID.randomUUID().toString()
        // Character
        'character'              | 'a'
        // After
        'afterDate'              | null
        'afterDate'              | new Date(new Date().getTime() + 3000)
        'afterCalendar'          | null
        'afterCalendar'          | Calendar.instance.add(Calendar.SECOND, 3)
        'afterTemporal'          | null
        'afterTemporal'          | Instant.now().plusSeconds(10)
        'afterTemporal'          | LocalTime.now().plusSeconds(10)
        'afterTemporal'          | LocalDate.now().plusDays(1)
        'afterTemporal'          | LocalDateTime.now().plusSeconds(10)
        // AfterOrNow
        'afterOrNowDate'         | null
        'afterOrNowDate'         | { new Date(new Date().getTime()) }
        'afterOrNowDate'         | { new Date(new Date().getTime() + 3000) }
        'afterOrNowCalendar'     | null
        'afterOrNowCalendar'     | { Calendar.instance }
        'afterOrNowCalendar'     | { Calendar.instance.add(Calendar.SECOND, 3) }
        'afterOrNowTemporal'     | null
        'afterOrNowTemporal'     | { Instant.now() }
        'afterOrNowTemporal'     | { Instant.now().plusSeconds(10) }
        'afterOrNowTemporal'     | { LocalTime.now() }
        'afterOrNowTemporal'     | { LocalTime.now().plusSeconds(10) }
        'afterOrNowTemporal'     | { LocalDate.now() }
        'afterOrNowTemporal'     | { LocalDate.now().plusDays(1) }
        'afterOrNowTemporal'     | { LocalDateTime.now() }
        'afterOrNowTemporal'     | { LocalDateTime.now().plusSeconds(10) }
        // Before
        'beforeDate'             | null
        'beforeDate'             | new Date(new Date().getTime() - 2000)
        'beforeCalendar'         | null
        'beforeCalendar'         | Calendar.instance.add(Calendar.SECOND, -2)
        'beforeTemporal'         | null
        'beforeTemporal'         | Instant.now().minusSeconds(10)
        'beforeTemporal'         | LocalTime.now().minusSeconds(10)
        'beforeTemporal'         | LocalDate.now().minusDays(1)
        'beforeTemporal'         | LocalDateTime.now().minusSeconds(10)
        // BeforeOrNow
        'beforeOrNowDate'        | null
        'beforeOrNowDate'        | { new Date(new Date().getTime()) }
        'beforeOrNowDate'        | { new Date(new Date().getTime() - 2000) }
        'beforeOrNowCalendar'    | null
        'beforeOrNowCalendar'    | { Calendar.instance }
        'beforeOrNowCalendar'    | { Calendar.instance.add(Calendar.SECOND, -2) }
        'beforeOrNowTemporal'    | null
        'beforeOrNowTemporal'    | { Instant.now() }
        'beforeOrNowTemporal'    | { Instant.now().minusSeconds(10) }
        'beforeOrNowTemporal'    | { LocalTime.now() }
        'beforeOrNowTemporal'    | { LocalTime.now().minusSeconds(10) }
        'beforeOrNowTemporal'    | { LocalDate.now() }
        'beforeOrNowTemporal'    | { LocalDate.now().minusDays(1) }
        'beforeOrNowTemporal'    | { LocalDateTime.now() }
        'beforeOrNowTemporal'    | { LocalDateTime.now().minusSeconds(10) }
    }

    def 'test that validate of field #fieldName and value #value throws'() {
        given:
        def field = Fields.getDeclaredField(fieldName)

        when:
        validator.validate(field, value)

        then:
        def e = thrown(ValidationException)
        assert e.violations.size() > 0
        def first = e.violations[fieldName][0]
        def second = expectedViolations[0]
        assert first.value == second.value
        assert first.message == second.message
        assert first.exceptionMessage == second.exceptionMessage
        assert first.arguments == second.arguments

        where:
        fieldName                | value                                    || expectedViolations
        // NonNull
        'nonNull'                | null                                     || [new ConstraintViolation(null, 'error.validation.not-null', 'cannot be null', ['value': null])]
        // AssertFalse
        'assertFalse'            | true                                     || [new ConstraintViolation(true, 'error.validation.required-false', String.format('must be false', true), ['value': true])]
        'assertFalse'            | 'not-a-boolean'                          || [ConstraintViolation.invalidType('not-a-boolean', 'true or false')]
        // AssertTrue
        'assertTrue'             | false                                    || [new ConstraintViolation(false, 'error.validation.required-true', String.format('must be true', false), ['value': false])]
        'assertTrue'             | 42                                       || [ConstraintViolation.invalidType(42, 'true or false')]
        // Max
        'max'                    | 1                                        || [new ConstraintViolation(1, 'error.validation.number-too-big', String.format('must be at most %2$s', 1, 0), ['value': 1, 'expected': 0])]
        'max'                    | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-too-big', String.format('must be at most %2$s', Integer.MAX_VALUE, 0), ['value': Integer.MAX_VALUE, 'expected': 0])]
        'max'                    | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Max Duration
        'maxDuration'            | Duration.ofMillis(1)                     || [new ConstraintViolation(Duration.ofMillis(1), 'error.validation.number-too-big', String.format('must be at most %2$s', Duration.ofMillis(1), 0), ['value': Duration.ofMillis(1), 'expected': 0])]
        'maxDuration'            | Duration.ofMillis(Integer.MAX_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MAX_VALUE), 'error.validation.number-too-big', String.format('must be at most %2$s', Duration.ofMillis(Integer.MAX_VALUE), 0), ['value': Duration.ofMillis(Integer.MAX_VALUE), 'expected': 0])]
        'maxDuration'            | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Max Character
        'maxCharacter'           | 'z' as char                              || [new ConstraintViolation('z' as char, 'error.validation.character-too-big', String.format('must be at most \'%2$s\'', 'z' as char, Z), ['value': 'z' as char, 'expected': Z])]
        'maxCharacter'           | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'character')]
        // NegativeOrZero
        'negativeOrZero'         | 1                                        || [new ConstraintViolation(1, 'error.validation.negative-or-zero', String.format('must be negative or zero', 1), ['value': 1])]
        'negativeOrZero'         | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative-or-zero', String.format('must be negative or zero', Integer.MAX_VALUE), ['value': Integer.MAX_VALUE])]
        'negativeOrZero'         | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // NegativeOrZero Duration
        'negativeOrZeroDuration' | Duration.ofMillis(1)                     || [new ConstraintViolation(Duration.ofMillis(1), 'error.validation.negative-or-zero', String.format('must be negative or zero', Duration.ofMillis(1)), ['value': Duration.ofMillis(1)])]
        'negativeOrZeroDuration' | Duration.ofMillis(Integer.MAX_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MAX_VALUE), 'error.validation.negative-or-zero', String.format('must be negative or zero', Duration.ofMillis(Integer.MAX_VALUE)), ['value': Duration.ofMillis(Integer.MAX_VALUE)])]
        'negativeOrZeroDuration' | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Negative
        'negative'               | 0                                        || [new ConstraintViolation(0, 'error.validation.negative', String.format('must be negative', 0), ['value': 0])]
        'negative'               | 1                                        || [new ConstraintViolation(1, 'error.validation.negative', String.format('must be negative', 1), ['value': 1])]
        'negative'               | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative', String.format('must be negative', Integer.MAX_VALUE), ['value': Integer.MAX_VALUE])]
        'negative'               | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Negative Duration
        'negativeDuration'       | Duration.ofMillis(0)                     || [new ConstraintViolation(Duration.ofMillis(0), 'error.validation.negative', String.format('must be negative', Duration.ofMillis(0)), ['value': Duration.ofMillis(0)])]
        'negativeDuration'       | Duration.ofMillis(1)                     || [new ConstraintViolation(Duration.ofMillis(1), 'error.validation.negative', String.format('must be negative', Duration.ofMillis(1)), ['value': Duration.ofMillis(1)])]
        'negativeDuration'       | Duration.ofMillis(Integer.MAX_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MAX_VALUE), 'error.validation.negative', String.format('must be negative', Duration.ofMillis(Integer.MAX_VALUE)), ['value': Duration.ofMillis(Integer.MAX_VALUE)])]
        'negativeDuration'       | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Min
        'min'                    | -1                                       || [new ConstraintViolation(-1, 'error.validation.number-too-small', String.format('must be at least %2$s', -1, 0), ['value': -1, 'expected': 0])]
        'min'                    | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-too-small', String.format('must be at least %2$s', Integer.MIN_VALUE, 0), ['value': Integer.MIN_VALUE, 'expected': 0])]
        'min'                    | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Min Duration
        'minDuration'            | Duration.ofMillis(-1)                    || [new ConstraintViolation(Duration.ofMillis(-1), 'error.validation.number-too-small', String.format('must be at least %2$s', Duration.ofMillis(-1), 0), ['value': Duration.ofMillis(-1), 'expected': 0])]
        'minDuration'            | Duration.ofMillis(Integer.MIN_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MIN_VALUE), 'error.validation.number-too-small', String.format('must be at least %2$s', Duration.ofMillis(Integer.MIN_VALUE), 0), ['value': Duration.ofMillis(Integer.MIN_VALUE), 'expected': 0])]
        'minDuration'            | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Min Character
        'minCharacter'           | '\n' as char                             || [new ConstraintViolation('\n' as char, 'error.validation.character-too-small', String.format('must be at least \'%2$s\'', '\n' as char, A), ['value': '\n' as char, 'expected': A])]
        'minCharacter'           | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'character')]
        // PositiveOrZero
        'positiveOrZero'         | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive-or-zero', String.format('must be positive or zero', -1), ['value': -1])]
        'positiveOrZero'         | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive-or-zero', String.format('must be positive or zero', Integer.MIN_VALUE), ['value': Integer.MIN_VALUE])]
        'positiveOrZero'         | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // PositiveOrZero Duration
        'positiveOrZeroDuration' | Duration.ofMillis(-1)                    || [new ConstraintViolation(Duration.ofMillis(-1), 'error.validation.positive-or-zero', String.format('must be positive or zero', Duration.ofMillis(-1)), ['value': Duration.ofMillis(-1)])]
        'positiveOrZeroDuration' | Duration.ofMillis(Integer.MIN_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MIN_VALUE), 'error.validation.positive-or-zero', String.format('must be positive or zero', Duration.ofMillis(Integer.MIN_VALUE)), ['value': Duration.ofMillis(Integer.MIN_VALUE)])]
        'positiveOrZeroDuration' | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Positive
        'positive'               | 0                                        || [new ConstraintViolation(0, 'error.validation.positive', String.format('must be positive', 0), ['value': 0])]
        'positive'               | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive', String.format('must be positive', -1), ['value': -1])]
        'positive'               | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive', String.format('must be positive', Integer.MIN_VALUE), ['value': Integer.MIN_VALUE])]
        'positive'               | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Positive Duration
        'positiveDuration'       | Duration.ofMillis(0)                     || [new ConstraintViolation(Duration.ofMillis(0), 'error.validation.positive', String.format('must be positive', Duration.ofMillis(0)), ['value': Duration.ofMillis(0)])]
        'positiveDuration'       | Duration.ofMillis(-1)                    || [new ConstraintViolation(Duration.ofMillis(-1), 'error.validation.positive', String.format('must be positive', Duration.ofMillis(-1)), ['value': Duration.ofMillis(-1)])]
        'positiveDuration'       | Duration.ofMillis(Integer.MIN_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MIN_VALUE), 'error.validation.positive', String.format('must be positive', Duration.ofMillis(Integer.MIN_VALUE)), ['value': Duration.ofMillis(Integer.MIN_VALUE)])]
        'positiveDuration'       | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Range
        'range'                  | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Integer.MIN_VALUE, 10.5, 0.5), ['value': Integer.MIN_VALUE, 'max': 10.5, 'min': 0.5])]
        'range'                  | 0                                        || [new ConstraintViolation(0, 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', 0, 10.5, 0.5), ['value': 0, 'max': 10.5, 'min': 0.5])]
        'range'                  | 11                                       || [new ConstraintViolation(11, 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', 11, 10.5, 0.5), ['value': 11, 'max': 10.5, 'min': 0.5])]
        'range'                  | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Integer.MAX_VALUE, 10.5, 0.5), ['value': Integer.MAX_VALUE, 'max': 10.5, 'min': 0.5])]
        'range'                  | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Range Duration
        'rangeDuration'          | Duration.ofMillis(Integer.MIN_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MIN_VALUE), 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Duration.ofMillis(Integer.MIN_VALUE), 10.5, 0.5), ['value': Duration.ofMillis(Integer.MIN_VALUE), 'max': 10.5, 'min': 0.5])]
        'rangeDuration'          | Duration.ofMillis(0)                     || [new ConstraintViolation(Duration.ofMillis(0), 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Duration.ofMillis(0), 10.5, 0.5), ['value': Duration.ofMillis(0), 'max': 10.5, 'min': 0.5])]
        'rangeDuration'          | Duration.ofMillis(11)                    || [new ConstraintViolation(Duration.ofMillis(11), 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Duration.ofMillis(11), 10.5, 0.5), ['value': Duration.ofMillis(11), 'max': 10.5, 'min': 0.5])]
        'rangeDuration'          | Duration.ofMillis(Integer.MAX_VALUE)     || [new ConstraintViolation(Duration.ofMillis(Integer.MAX_VALUE), 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', Duration.ofMillis(Integer.MAX_VALUE), 10.5, 0.5), ['value': Duration.ofMillis(Integer.MAX_VALUE), 'max': 10.5, 'min': 0.5])]
        'rangeDuration'          | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Range Character
        'rangeCharacter'         | '\n' as char                             || [new ConstraintViolation('\n' as char, 'error.validation.character-exceeds-range', String.format('must be at least \'%3$s\' and at most \'%2$s\'', '\n' as char, Z, A), ['value': '\n' as char, 'max': Z, 'min': A])]
        'rangeCharacter'         | 'z' as char                              || [new ConstraintViolation('z' as char, 'error.validation.character-exceeds-range', String.format('must be at least \'%3$s\' and at most \'%2$s\'', 'z' as char, Z, A), ['value': 'z' as char, 'max': Z, 'min': A])]
        'rangeCharacter'         | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'character')]
        // Port
        'port'                   | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.invalid-port', String.format('%1$s is not a valid port', Integer.MIN_VALUE), ['value': Integer.MIN_VALUE])]
        'port'                   | 0                                        || [new ConstraintViolation(0, 'error.validation.invalid-port', String.format('%1$s is not a valid port', 0), ['value': 0])]
        'port'                   | 65536                                    || [new ConstraintViolation(65536, 'error.validation.invalid-port', String.format('%1$s is not a valid port', 65536), ['value': 65536])]
        'port'                   | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.invalid-port', String.format('%1$s is not a valid port', Integer.MAX_VALUE), ['value': Integer.MAX_VALUE])]
        'port'                   | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Size (String)
        'sizeString'             | ''                                       || [new ConstraintViolation('', 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', '', 5, 1), ['value': '', 'max': 5, 'min': 1])]
        'sizeString'             | 'a'.repeat(6)                            || [new ConstraintViolation('a'.repeat(6), 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', 'a'.repeat(6), 5, 1), ['value': 'a'.repeat(6), 'max': 5, 'min': 1])]
        // Size (array)
        'sizeArray'              | noValuesArray                            || [new ConstraintViolation(noValuesArray, 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', Arrays.toString(noValuesArray), 5, 1), ['value': noValuesArray, 'max': 5, 'min': 1])]
        'sizeArray'              | exceedValuesArray                        || [new ConstraintViolation(exceedValuesArray, 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', Arrays.toString(exceedValuesArray), 5, 1), ['value': exceedValuesArray, 'max': 5, 'min': 1])]
        // Size (Collection)
        'sizeCollection'         | []                                       || [new ConstraintViolation([], 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', [], 5, 1), ['value': [], 'max': 5, 'min': 1])]
        'sizeCollection'         | (1..6).toList()                          || [new ConstraintViolation((1..6).toList(), 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', (1..6).toList(), 5, 1), ['value': (1..6).toList(), 'max': 5, 'min': 1])]
        // Size (Map)
        'sizeMap'                | [:]                                      || [new ConstraintViolation([:], 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', [:], 5, 1), ['value': [:], 'max': 5, 'min': 1])]
        'sizeMap'                | (1..6).collectEntries { it -> [it, it] } || [new ConstraintViolation((1..6).collectEntries { it -> [it, it] }, 'error.validation.argument-exceeds-size', String.format('size must be at least %3$s and at most %2$s elements long', (1..6).collectEntries { it -> [it, it] }, 5, 1), ['value': (1..6).collectEntries { it -> [it, it] }, 'max': 5, 'min': 1])]
        // Matches
        'matches'                | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-string', String.format('\'%1$s\' does not match regex \'%2$s\'', '', '[A-Za-z]+'), ['value': '', 'expected': '[A-Za-z]+'])]
        'matches'                | 'Alessandro!'                            || [new ConstraintViolation('Alessandro!', 'error.validation.invalid-string', String.format('\'%1$s\' does not match regex \'%2$s\'', 'Alessandro!', '[A-Za-z]+'), ['value': 'Alessandro!', 'expected': '[A-Za-z]+'])]
        'matches'                | '01001'                                  || [new ConstraintViolation('01001', 'error.validation.invalid-string', String.format('\'%1$s\' does not match regex \'%2$s\'', '01001', '[A-Za-z]+'), ['value': '01001', 'expected': '[A-Za-z]+'])]
        'matches'                | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Port + @Range (minPort)
        'minPort'                | 1007                                     || [new ConstraintViolation(1007, 'error.validation.number-exceeds-range', String.format('must be at least %3$s and at most %2$s', 1007, 100, 1), ['value': 1007, 'max': 100, 'min': 1])]
        'minPort'                | 'hello'                                  || [ConstraintViolation.invalidType('hello', 'number or time duration')]
        // Hostname
        'hostname'               | '-invalid.com'                           || [new ConstraintViolation('-invalid.com', 'error.validation.invalid-hostname', String.format('\'%1$s\' is not a valid hostname', '-invalid.com'), ['value': '-invalid.com'])]
        'hostname'               | 'trailing-.com'                          || [new ConstraintViolation('trailing-.com', 'error.validation.invalid-hostname', String.format('\'%1$s\' is not a valid hostname', 'trailing-.com'), ['value': 'trailing-.com'])]
        'hostname'               | 'example..com'                           || [new ConstraintViolation('example..com', 'error.validation.invalid-hostname', String.format('\'%1$s\' is not a valid hostname', 'example..com'), ['value': 'example..com'])]
        'hostname'               | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Email
        'email'                  | 'notanemail'                             || [new ConstraintViolation('notanemail', 'error.validation.invalid-email', String.format('\'%1$s\' is not a valid email', 'notanemail'), ['value': 'notanemail'])]
        'email'                  | '@domain.com'                            || [new ConstraintViolation('@domain.com', 'error.validation.invalid-email', String.format('\'%1$s\' is not a valid email', '@domain.com'), ['value': '@domain.com'])]
        'email'                  | 'user@'                                  || [new ConstraintViolation('user@', 'error.validation.invalid-email', String.format('\'%1$s\' is not a valid email', 'user@'), ['value': 'user@'])]
        'email'                  | 'user@domain'                            || [new ConstraintViolation('user@domain', 'error.validation.invalid-email', String.format('\'%1$s\' is not a valid email', 'user@domain'), ['value': 'user@domain'])]
        'email'                  | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // IPv4
        'ipv4'                   | '256.1'                                  || [new ConstraintViolation('256.1', 'error.validation.invalid-ipv4', String.format('\'%1$s\' is not a valid IPv4', '256.1'), ['value': '256.1'])]
        'ipv4'                   | '192.168.1'                              || [new ConstraintViolation('192.168.1', 'error.validation.invalid-ipv4', String.format('\'%1$s\' is not a valid IPv4', '192.168.1'), ['value': '192.168.1'])]
        'ipv4'                   | 'not.an.ip.addr'                         || [new ConstraintViolation('not.an.ip.addr', 'error.validation.invalid-ipv4', String.format('\'%1$s\' is not a valid IPv4', 'not.an.ip.addr'), ['value': 'not.an.ip.addr'])]
        'ipv4'                   | '192.168.1.1.1'                          || [new ConstraintViolation('192.168.1.1.1', 'error.validation.invalid-ipv4', String.format('\'%1$s\' is not a valid IPv4', '192.168.1.1.1'), ['value': '192.168.1.1.1'])]
        'ipv4'                   | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // IPv6
        'ipv6'                   | '192.168.1.1'                            || [new ConstraintViolation('192.168.1.1', 'error.validation.invalid-ipv6', String.format('\'%1$s\' is not a valid IPv6', '192.168.1.1'), ['value': '192.168.1.1'])]
        'ipv6'                   | 'gggg::1'                                || [new ConstraintViolation('gggg::1', 'error.validation.invalid-ipv6', String.format('\'%1$s\' is not a valid IPv6', 'gggg::1'), ['value': 'gggg::1'])]
        'ipv6'                   | '2001:0db8:85a3:0000:0000:8a2e:0370'     || [new ConstraintViolation('2001:0db8:85a3:0000:0000:8a2e:0370', 'error.validation.invalid-ipv6', String.format('\'%1$s\' is not a valid IPv6', '2001:0db8:85a3:0000:0000:8a2e:0370'), ['value': '2001:0db8:85a3:0000:0000:8a2e:0370'])]
        'ipv6'                   | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Url
        'url'                    | 'example.com'                            || [new ConstraintViolation('example.com', 'error.validation.invalid-url', String.format('\'%1$s\' is not a valid URL', 'example.com'), ['value': 'example.com'])]
        'url'                    | 'not-a-url'                              || [new ConstraintViolation('not-a-url', 'error.validation.invalid-url', String.format('\'%1$s\' is not a valid URL', 'not-a-url'), ['value': 'not-a-url'])]
        'url'                    | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // HexColor
        'hexColor'               | 'FF0000'                                 || [new ConstraintViolation('FF0000', 'error.validation.invalid-hex-color', String.format('\'%1$s\' is not a valid HEX color', 'FF0000'), ['value': 'FF0000'])]
        'hexColor'               | '#GGG000'                                || [new ConstraintViolation('#GGG000', 'error.validation.invalid-hex-color', String.format('\'%1$s\' is not a valid HEX color', '#GGG000'), ['value': '#GGG000'])]
        'hexColor'               | '#GG00'                                  || [new ConstraintViolation('#GG00', 'error.validation.invalid-hex-color', String.format('\'%1$s\' is not a valid HEX color', '#GG00'), ['value': '#GG00'])]
        'hexColor'               | '#GG00GG00'                              || [new ConstraintViolation('#GG00GG00', 'error.validation.invalid-hex-color', String.format('\'%1$s\' is not a valid HEX color', '#GG00GG00'), ['value': '#GG00GG00'])]
        'hexColor'               | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Identifier
        'identifier'             | '1invalid'                               || [new ConstraintViolation('1invalid', 'error.validation.invalid-identifier', String.format('\'%1$s\' is not a valid identifier', '1invalid'), ['value': '1invalid'])]
        'identifier'             | 'my-var'                                 || [new ConstraintViolation('my-var', 'error.validation.invalid-identifier', String.format('\'%1$s\' is not a valid identifier', 'my-var'), ['value': 'my-var'])]
        'identifier'             | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-identifier', String.format('\'%1$s\' is not a valid identifier', ''), ['value': ''])]
        'identifier'             | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Alphabetical
        'alphabetical'           | 'Hello1'                                 || [new ConstraintViolation('Hello1', 'error.validation.invalid-alphabetical', String.format('\'%1$s\' is not allowed (only letters)', 'Hello1'), ['value': 'Hello1'])]
        'alphabetical'           | 'hello world'                            || [new ConstraintViolation('hello world', 'error.validation.invalid-alphabetical', String.format('\'%1$s\' is not allowed (only letters)', 'hello world'), ['value': 'hello world'])]
        'alphabetical'           | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-alphabetical', String.format('\'%1$s\' is not allowed (only letters)', ''), ['value': ''])]
        'alphabetical'           | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // AlphabeticalOrDigit
        'alphabeticalOrDigit'    | 'Hello!'                                 || [new ConstraintViolation('Hello!', 'error.validation.invalid-alphabetical-or-digit', String.format('\'%1$s\' is not allowed (only letters and digits)', 'Hello!'), ['value': 'Hello!'])]
        'alphabeticalOrDigit'    | 'user name'                              || [new ConstraintViolation('user name', 'error.validation.invalid-alphabetical-or-digit', String.format('\'%1$s\' is not allowed (only letters and digits)', 'user name'), ['value': 'user name'])]
        'alphabeticalOrDigit'    | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-alphabetical-or-digit', String.format('\'%1$s\' is not allowed (only letters and digits)', ''), ['value': ''])]
        'alphabeticalOrDigit'    | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // NotBlank
        'notBlank'               | '        '                               || [new ConstraintViolation('        ', 'error.validation.not-blank', String.format('must at least contain one non-space character', '        '), ['value': '        '])]
        'notBlank'               | ''                                       || [new ConstraintViolation('', 'error.validation.not-blank', String.format('must at least contain one non-space character', ''), ['value': ''])]
        'notBlank'               | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // NotEmpty
        'notEmpty'               | ''                                       || [new ConstraintViolation('', 'error.validation.not-empty', 'cannot be empty', ['value': ''])]
        'notEmpty'               | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Uuid
        'uuid'                   | 'Hello, world!'                          || [new ConstraintViolation('Hello, world!', null, "Invalid value for annotation ${Uuid.simpleName}: Hello, world!", ['value': 'Hello, world!', 'regex': 'DEFAULT'])]
        'uuid'                   | ''                                       || [new ConstraintViolation('', null, "Invalid value for annotation ${Uuid.simpleName}: ", ['value': '', 'regex': 'DEFAULT'])]
        'uuid'                   | 42                                       || [ConstraintViolation.invalidType(42, 'string (or any character sequence)')]
        // Character
        'character'              | 'Hello, world!'                          || [new ConstraintViolation('Hello, world!', 'error.validation.invalid-character', String.format('\'%1$s\' is not a valid character', 'Hello, world!'), ['value': 'Hello, world!'])]
        'character'              | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-character', String.format('\'%1$s\' is not a valid character', ''), ['value': ''])]
        'character'              | 42                                       || [ConstraintViolation.invalidType(42, "$CharSequence.canonicalName")]
        // After
        'afterDate'              | new Date(0)                              || [new ConstraintViolation(new Date(0), 'error.validation.not-after', 'must be after now', ['value': new Date(0)])]
        'afterDate'              | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'afterCalendar'          | new GregorianCalendar(2000, 0, 1)        || [new ConstraintViolation(new GregorianCalendar(2000, 0, 1), 'error.validation.not-after', 'must be after now', ['value': new GregorianCalendar(2000, 0, 1)])]
        'afterCalendar'          | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'afterTemporal'          | Instant.ofEpochMilli(0)                  || [new ConstraintViolation(Instant.ofEpochMilli(0), 'error.validation.not-after', 'must be after now', ['value': Instant.ofEpochMilli(0)])]
        'afterTemporal'          | LocalTime.of(0, 0)                       || [new ConstraintViolation(LocalTime.of(0, 0), 'error.validation.not-after', 'must be after now', ['value': LocalTime.of(0, 0)])]
        'afterTemporal'          | LocalDate.of(2000, 1, 1)                 || [new ConstraintViolation(LocalDate.of(2000, 1, 1), 'error.validation.not-after', 'must be after now', ['value': LocalDate.of(2000, 1, 1)])]
        'afterTemporal'          | LocalDateTime.of(2000, 1, 1, 0, 0)       || [new ConstraintViolation(LocalDateTime.of(2000, 1, 1, 0, 0), 'error.validation.not-after', 'must be after now', ['value': LocalDateTime.of(2000, 1, 1, 0, 0)])]
        'afterTemporal'          | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        // AfterOrNow
        'afterOrNowDate'         | new Date(0)                              || [new ConstraintViolation(new Date(0), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': new Date(0)])]
        'afterOrNowDate'         | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'afterOrNowCalendar'     | new GregorianCalendar(2000, 0, 1)        || [new ConstraintViolation(new GregorianCalendar(2000, 0, 1), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': new GregorianCalendar(2000, 0, 1)])]
        'afterOrNowCalendar'     | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'afterOrNowTemporal'     | Instant.ofEpochMilli(0)                  || [new ConstraintViolation(Instant.ofEpochMilli(0), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': Instant.ofEpochMilli(0)])]
        'afterOrNowTemporal'     | LocalTime.of(0, 0)                       || [new ConstraintViolation(LocalTime.of(0, 0), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': LocalTime.of(0, 0)])]
        'afterOrNowTemporal'     | LocalDate.of(2000, 1, 1)                 || [new ConstraintViolation(LocalDate.of(2000, 1, 1), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': LocalDate.of(2000, 1, 1)])]
        'afterOrNowTemporal'     | LocalDateTime.of(2000, 1, 1, 0, 0)       || [new ConstraintViolation(LocalDateTime.of(2000, 1, 1, 0, 0), 'error.validation.not-after-or-now', 'must be now or in the future', ['value': LocalDateTime.of(2000, 1, 1, 0, 0)])]
        'afterOrNowTemporal'     | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        // Before
        'beforeDate'             | new Date(4102444800000L)                 || [new ConstraintViolation(new Date(4102444800000L), 'error.validation.not-before', 'must be before now', ['value': new Date(4102444800000L)])]
        'beforeDate'             | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'beforeCalendar'         | new GregorianCalendar(2100, 0, 1)        || [new ConstraintViolation(new GregorianCalendar(2100, 0, 1), 'error.validation.not-before', 'must be before now', ['value': new GregorianCalendar(2100, 0, 1)])]
        'beforeCalendar'         | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'beforeTemporal'         | Instant.ofEpochSecond(4102444800L)       || [new ConstraintViolation(Instant.ofEpochSecond(4102444800L), 'error.validation.not-before', 'must be before now', ['value': Instant.ofEpochSecond(4102444800L)])]
        'beforeTemporal'         | LocalTime.of(23, 59, 59)                 || [new ConstraintViolation(LocalTime.of(23, 59, 59), 'error.validation.not-before', 'must be before now', ['value': LocalTime.of(23, 59, 59)])]
        'beforeTemporal'         | LocalDate.of(2100, 1, 1)                 || [new ConstraintViolation(LocalDate.of(2100, 1, 1), 'error.validation.not-before', 'must be before now', ['value': LocalDate.of(2100, 1, 1)])]
        'beforeTemporal'         | LocalDateTime.of(2100, 1, 1, 0, 0)       || [new ConstraintViolation(LocalDateTime.of(2100, 1, 1, 0, 0), 'error.validation.not-before', 'must be before now', ['value': LocalDateTime.of(2100, 1, 1, 0, 0)])]
        'beforeTemporal'         | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        // BeforeOrNow
        'beforeOrNowDate'        | new Date(4102444800000L)                 || [new ConstraintViolation(new Date(4102444800000L), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': new Date(4102444800000L)])]
        'beforeOrNowDate'        | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'beforeOrNowCalendar'    | new GregorianCalendar(2100, 0, 1)        || [new ConstraintViolation(new GregorianCalendar(2100, 0, 1), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': new GregorianCalendar(2100, 0, 1)])]
        'beforeOrNowCalendar'    | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
        'beforeOrNowTemporal'    | Instant.ofEpochSecond(4102444800L)       || [new ConstraintViolation(Instant.ofEpochSecond(4102444800L), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': Instant.ofEpochSecond(4102444800L)])]
        'beforeOrNowTemporal'    | LocalTime.of(23, 59, 59)                 || [new ConstraintViolation(LocalTime.of(23, 59, 59), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': LocalTime.of(23, 59, 59)])]
        'beforeOrNowTemporal'    | LocalDate.of(2100, 1, 1)                 || [new ConstraintViolation(LocalDate.of(2100, 1, 1), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': LocalDate.of(2100, 1, 1)])]
        'beforeOrNowTemporal'    | LocalDateTime.of(2100, 1, 1, 0, 0)       || [new ConstraintViolation(LocalDateTime.of(2100, 1, 1, 0, 0), 'error.validation.not-before-or-now', 'must be now or in the past', ['value': LocalDateTime.of(2100, 1, 1, 0, 0)])]
        'beforeOrNowTemporal'    | 42                                       || [ConstraintViolation.invalidType(42, 'time')]
    }

}