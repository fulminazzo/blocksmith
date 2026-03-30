//file:noinspection unused
package it.fulminazzo.blocksmith.validation

import it.fulminazzo.blocksmith.validation.annotation.*
import spock.lang.Specification

class ValidatorTest extends Specification {
    private static final Validator validator = Validator.instance

    private static final noValuesArray = new Object[0]
    private static final exceedValuesArray = (1..6).toArray()

    @NonNull
    private Object nonNull
    @AssertFalse
    private boolean assertFalse
    @AssertTrue
    private boolean assertTrue
    @Max(0)
    private int max
    @NegativeOrZero
    private int negativeOrZero
    @Negative
    private int negative
    @Min(0)
    private int min
    @PositiveOrZero
    private int positiveOrZero
    @Positive
    private int positive
    @Range(min = 1, max = 10)
    private int range
    @Port
    private int port
    @Size(min = 1, max = 5)
    private String sizeString
    @Size(min = 1, max = 5)
    private Object[] sizeArray
    @Size(min = 1, max = 5)
    private Collection sizeCollection
    @Size(min = 1, max = 5)
    private Map sizeMap
    @Matches('[A-Za-z]+')
    private String matches
    @Hostname
    private String hostname
    @Email
    private String email
    @IPv4
    private String ipv4
    @IPv6
    private String ipv6
    @Url
    private String url
    @HexColor
    private String hexColor
    @Identifier
    private String identifier
    @Alphabetical
    private String alphabetical
    @AlphabeticalOrDigit
    private String alphabeticalOrDigit
    @NotBlank
    private String notBlank
    @NotEmpty
    private String notEmpty
    @Port
    @Range(min = 1, max = 100)
    private int minPort

    def 'test that validate of field #fieldName and value #value does not throw'() {
        given:
        def field = ValidatorTest.getDeclaredField(fieldName)

        when:
        validator.validate(field, value)

        then:
        noExceptionThrown()

        where:
        fieldName             | value
        'nonNull'             | new Object()
        'assertFalse'         | null
        'assertFalse'         | false
        'assertTrue'          | null
        'assertTrue'          | true
        'max'                 | null
        'max'                 | 0
        'max'                 | -1
        'max'                 | Integer.MIN_VALUE
        'negativeOrZero'      | null
        'negativeOrZero'      | 0
        'negativeOrZero'      | -1
        'negativeOrZero'      | Integer.MIN_VALUE
        'negative'            | null
        'negative'            | -1
        'negative'            | Integer.MIN_VALUE
        'min'                 | null
        'min'                 | 0
        'min'                 | 1
        'min'                 | Integer.MAX_VALUE
        'positiveOrZero'      | null
        'positiveOrZero'      | 0
        'positiveOrZero'      | 1
        'positiveOrZero'      | Integer.MAX_VALUE
        'positive'            | null
        'positive'            | 1
        'positive'            | Integer.MAX_VALUE
        'range'               | null
        'range'               | 1
        'range'               | 10
        'port'                | null
        'port'                | 0
        'port'                | 65535
        'sizeString'          | null
        'sizeString'          | 'a'
        'sizeString'          | 'a'.repeat(5)
        'sizeArray'           | null
        'sizeArray'           | ['a'].toArray()
        'sizeArray'           | (1..5).toArray()
        'sizeCollection'      | null
        'sizeCollection'      | ['a']
        'sizeCollection'      | (1..5).toList()
        'sizeMap'             | null
        'sizeMap'             | ['a']
        'sizeMap'             | (1..5).collectEntries { it -> [it, it] }
        'matches'             | null
        'matches'             | 'a'
        'matches'             | 'Alessandro'
        'hostname'            | null
        'hostname'            | 'localhost'
        'hostname'            | 'example.com'
        'hostname'            | 'my-server.local'
        'hostname'            | 'sub.domain.co.uk'
        'email'               | null
        'email'               | 'user@example.com'
        'email'               | 'name.surname+tag@sub.domain.org'
        'email'               | 'user123@my-domain.io'
        'ipv4'                | null
        'ipv4'                | '127.0.0.1'
        'ipv4'                | '0.0.0.0'
        'ipv4'                | '255.255.255.255'
        'ipv4'                | '192.168.1.100'
        'ipv6'                | null
        'ipv6'                | '2001:0db8:85a3:0000:0000:8a2e:0370:7334'
        'ipv6'                | '::1'
        'ipv6'                | '::'
        'ipv6'                | 'fe80::1'
        'url'                 | null
        'url'                 | 'http://example.com'
        'url'                 | 'https://sub.domain.com/path?query=1#anchor'
        'url'                 | 'https://my-site.org:8080/api/v1'
        'hexColor'            | null
        'hexColor'            | '#FF0000'
        'hexColor'            | '#abc123'
        'hexColor'            | '#000000'
        'hexColor'            | '#FFFFFF'
        'identifier'          | null
        'identifier'          | 'myVariable'
        'identifier'          | '_private'
        'identifier'          | 'a1'
        'alphabetical'        | null
        'alphabetical'        | 'Hello'
        'alphabetical'        | 'world'
        'alphabetical'        | 'Alessandro'
        'alphabeticalOrDigit' | null
        'alphabeticalOrDigit' | 'Hello123'
        'alphabeticalOrDigit' | 'abc'
        'alphabeticalOrDigit' | '12345'
        'notBlank'            | null
        'notBlank'            | 'hello'
        'notBlank'            | 'Hello, world!'
        'notBlank'            | 'a'
        'notEmpty'            | null
        'notEmpty'            | 'hello'
        'notEmpty'            | ' '
    }

    def 'test that validate of field #fieldName and value #value throws'() {
        given:
        def field = ValidatorTest.getDeclaredField(fieldName)

        when:
        validator.validate(field, value)

        then:
        def e = thrown(ValidationException)
        e.violations == expectedViolations.toSet()

        where:
        fieldName             | value                                    || expectedViolations
        'nonNull'             | null                                     || [new ConstraintViolation(null, 'error.validation.not-null', NonNull.DEFAULT_MESSAGE)]
        'assertFalse'         | true                                     || [new ConstraintViolation(true, 'error.validation.required-false', String.format(AssertFalse.DEFAULT_MESSAGE, true))]
        'assertTrue'          | false                                    || [new ConstraintViolation(false, 'error.validation.required-true', String.format(AssertTrue.DEFAULT_MESSAGE, false))]
        'max'                 | 1                                        || [new ConstraintViolation(1, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, 1, 0.0))]
        'max'                 | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, Integer.MAX_VALUE, 0.0))]
        'negativeOrZero'      | 1                                        || [new ConstraintViolation(1, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, 1))]
        'negativeOrZero'      | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'negative'            | 0                                        || [new ConstraintViolation(0, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 0))]
        'negative'            | 1                                        || [new ConstraintViolation(1, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 1))]
        'negative'            | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'min'                 | -1                                       || [new ConstraintViolation(-1, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, -1, 0.0))]
        'min'                 | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, Integer.MIN_VALUE, 0.0))]
        'positiveOrZero'      | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, -1))]
        'positiveOrZero'      | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'positive'            | 0                                        || [new ConstraintViolation(0, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, 0))]
        'positive'            | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, -1))]
        'positive'            | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'range'               | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MIN_VALUE, 10.0, 1.0))]
        'range'               | 0                                        || [new ConstraintViolation(0, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 0, 10.0, 1.0))]
        'range'               | 11                                       || [new ConstraintViolation(11, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 11, 10.0, 1.0))]
        'range'               | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MAX_VALUE, 10.0, 1.0))]
        'port'                | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'port'                | -1                                       || [new ConstraintViolation(-1, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, -1))]
        'port'                | 65536                                    || [new ConstraintViolation(65536, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, 65536))]
        'port'                | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'sizeString'          | ''                                       || [new ConstraintViolation('', 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, '', 5, 1))]
        'sizeString'          | 'a'.repeat(6)                            || [new ConstraintViolation('a'.repeat(6), 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, 'a'.repeat(6), 5, 1))]
        'sizeArray'           | noValuesArray                            || [new ConstraintViolation(noValuesArray, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, noValuesArray, 5, 1))]
        'sizeArray'           | exceedValuesArray                        || [new ConstraintViolation(exceedValuesArray, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, exceedValuesArray, 5, 1))]
        'sizeCollection'      | []                                       || [new ConstraintViolation([], 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, [], 5, 1))]
        'sizeCollection'      | (1..6).toList()                          || [new ConstraintViolation((1..6).toList(), 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, (1..6).toList(), 5, 1))]
        'sizeMap'             | [:]                                      || [new ConstraintViolation([:], 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, [:], 5, 1))]
        'sizeMap'             | (1..6).collectEntries { it -> [it, it] } ||
                [new ConstraintViolation((1..6).collectEntries { it -> [it, it] }, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, (1..6).collectEntries { it -> [it, it] }, 5, 1))]
        'matches'             | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, '', '[A-Za-z]+'))]
        'matches'             | 'Alessandro!'                            || [new ConstraintViolation('Alessandro!', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, 'Alessandro!', '[A-Za-z]+'))]
        'matches'             | '01001'                                  || [new ConstraintViolation('01001', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, '01001', '[A-Za-z]+'))]
        'minPort'             | 1007                                     || [new ConstraintViolation(1007, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 1007, 100.0, 1.0))]
        'hostname'            | '-invalid.com'                           || [new ConstraintViolation('-invalid.com', 'error.validation.invalid-hostname', String.format(Hostname.DEFAULT_MESSAGE, '-invalid.com'))]
        'hostname'            | 'trailing-.com'                          || [new ConstraintViolation('trailing-.com', 'error.validation.invalid-hostname', String.format(Hostname.DEFAULT_MESSAGE, 'trailing-.com'))]
        'hostname'            | 'example..com'                           || [new ConstraintViolation('example..com', 'error.validation.invalid-hostname', String.format(Hostname.DEFAULT_MESSAGE, 'example..com'))]
        'email'               | 'notanemail'                             || [new ConstraintViolation('notanemail', 'error.validation.invalid-email', String.format(Email.DEFAULT_MESSAGE, 'notanemail'))]
        'email'               | '@domain.com'                            || [new ConstraintViolation('@domain.com', 'error.validation.invalid-email', String.format(Email.DEFAULT_MESSAGE, '@domain.com'))]
        'email'               | 'user@'                                  || [new ConstraintViolation('user@', 'error.validation.invalid-email', String.format(Email.DEFAULT_MESSAGE, 'user@'))]
        'email'               | 'user@domain'                            || [new ConstraintViolation('user@domain', 'error.validation.invalid-email', String.format(Email.DEFAULT_MESSAGE, 'user@domain'))]
        'ipv4'                | '256.0.0.1'                              || [new ConstraintViolation('256.0.0.1', 'error.validation.invalid-ipv4', String.format(IPv4.DEFAULT_MESSAGE, '256.0.0.1'))]
        'ipv4'                | '192.168.1'                              || [new ConstraintViolation('192.168.1', 'error.validation.invalid-ipv4', String.format(IPv4.DEFAULT_MESSAGE, '192.168.1'))]
        'ipv4'                | 'not.an.ip.addr'                         || [new ConstraintViolation('not.an.ip.addr', 'error.validation.invalid-ipv4', String.format(IPv4.DEFAULT_MESSAGE, 'not.an.ip.addr'))]
        'ipv4'                | '192.168.1.1.1'                          || [new ConstraintViolation('192.168.1.1.1', 'error.validation.invalid-ipv4', String.format(IPv4.DEFAULT_MESSAGE, '192.168.1.1.1'))]
        'ipv6'                | '192.168.1.1'                            || [new ConstraintViolation('192.168.1.1', 'error.validation.invalid-ipv6', String.format(IPv6.DEFAULT_MESSAGE, '192.168.1.1'))]
        'ipv6'                | 'gggg::1'                                || [new ConstraintViolation('gggg::1', 'error.validation.invalid-ipv6', String.format(IPv6.DEFAULT_MESSAGE, 'gggg::1'))]
        'ipv6'                | '2001:0db8:85a3:0000:0000:8a2e:0370'     || [new ConstraintViolation('2001:0db8:85a3:0000:0000:8a2e:0370', 'error.validation.invalid-ipv6', String.format(IPv6.DEFAULT_MESSAGE, '2001:0db8:85a3:0000:0000:8a2e:0370'))]
        'url'                 | 'ftp://not-http.com'                     || [new ConstraintViolation('ftp://not-http.com', 'error.validation.invalid-url', String.format(Url.DEFAULT_MESSAGE, 'ftp://not-http.com'))]
        'url'                 | 'example.com'                            || [new ConstraintViolation('example.com', 'error.validation.invalid-url', String.format(Url.DEFAULT_MESSAGE, 'example.com'))]
        'url'                 | 'not-a-url'                              || [new ConstraintViolation('not-a-url', 'error.validation.invalid-url', String.format(Url.DEFAULT_MESSAGE, 'not-a-url'))]
        'hexColor'            | 'FF0000'                                 || [new ConstraintViolation('FF0000', 'error.validation.invalid-hex-color', String.format(HexColor.DEFAULT_MESSAGE, 'FF0000'))]
        'hexColor'            | '#GGG000'                                || [new ConstraintViolation('#GGG000', 'error.validation.invalid-hex-color', String.format(HexColor.DEFAULT_MESSAGE, '#GGG000'))]
        'hexColor'            | '#FF00'                                  || [new ConstraintViolation('#FF00', 'error.validation.invalid-hex-color', String.format(HexColor.DEFAULT_MESSAGE, '#FF00'))]
        'hexColor'            | '#FF00FF00'                              || [new ConstraintViolation('#FF00FF00', 'error.validation.invalid-hex-color', String.format(HexColor.DEFAULT_MESSAGE, '#FF00FF00'))]
        'identifier'          | '1invalid'                               || [new ConstraintViolation('1invalid', 'error.validation.invalid-identifier', String.format(Identifier.DEFAULT_MESSAGE, '1invalid'))]
        'identifier'          | 'my-var'                                 || [new ConstraintViolation('my-var', 'error.validation.invalid-identifier', String.format(Identifier.DEFAULT_MESSAGE, 'my-var'))]
        'identifier'          | 'a'                                      || [new ConstraintViolation('a', 'error.validation.invalid-identifier', String.format(Identifier.DEFAULT_MESSAGE, 'a'))]
        'identifier'          | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-identifier', String.format(Identifier.DEFAULT_MESSAGE, ''))]
        'alphabetical'        | 'Hello1'                                 || [new ConstraintViolation('Hello1', 'error.validation.invalid-alphabetical', String.format(Alphabetical.DEFAULT_MESSAGE, 'Hello1'))]
        'alphabetical'        | 'hello world'                            || [new ConstraintViolation('hello world', 'error.validation.invalid-alphabetical', String.format(Alphabetical.DEFAULT_MESSAGE, 'hello world'))]
        'alphabetical'        | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-alphabetical', String.format(Alphabetical.DEFAULT_MESSAGE, ''))]
        'alphabeticalOrDigit' | 'Hello!'                                 || [new ConstraintViolation('Hello!', 'error.validation.invalid-alphabetical-or-digit', String.format(AlphabeticalOrDigit.DEFAULT_MESSAGE, 'Hello!'))]
        'alphabeticalOrDigit' | 'user name'                              || [new ConstraintViolation('user name', 'error.validation.invalid-alphabetical-or-digit', String.format(AlphabeticalOrDigit.DEFAULT_MESSAGE, 'user name'))]
        'alphabeticalOrDigit' | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-alphabetical-or-digit', String.format(AlphabeticalOrDigit.DEFAULT_MESSAGE, ''))]
        'notBlank'            | '        '                               || [new ConstraintViolation('        ', 'error.validation.not-blank', String.format(NotBlank.DEFAULT_MESSAGE, '        '))]
        'notBlank'            | ''                                       || [new ConstraintViolation('', 'error.validation.not-blank', String.format(NotBlank.DEFAULT_MESSAGE, ''))]
        'notEmpty'            | ''                                       || [new ConstraintViolation('', 'error.validation.not-empty', NotEmpty.DEFAULT_MESSAGE)]
        'assertFalse'         | 'Hello, world'                            | []
    }

}