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
        fieldName        | value
        'nonNull'        | new Object()
        'assertFalse'    | null
        'assertFalse'    | false
        'assertTrue'     | null
        'assertTrue'     | true
        'max'            | null
        'max'            | 0
        'max'            | -1
        'max'            | Integer.MIN_VALUE
        'negativeOrZero' | null
        'negativeOrZero' | 0
        'negativeOrZero' | -1
        'negativeOrZero' | Integer.MIN_VALUE
        'negative'       | null
        'negative'       | -1
        'negative'       | Integer.MIN_VALUE
        'min'            | null
        'min'            | 0
        'min'            | 1
        'min'            | Integer.MAX_VALUE
        'positiveOrZero' | null
        'positiveOrZero' | 0
        'positiveOrZero' | 1
        'positiveOrZero' | Integer.MAX_VALUE
        'positive'       | null
        'positive'       | 1
        'positive'       | Integer.MAX_VALUE
        'range'          | null
        'range'          | 1
        'range'          | 10
        'port'           | null
        'port'           | 0
        'port'           | 65535
        'sizeString'     | null
        'sizeString'     | 'a'
        'sizeString'     | 'a'.repeat(5)
        'sizeArray'      | null
        'sizeArray'      | ['a'].toArray()
        'sizeArray'      | (1..5).toArray()
        'sizeCollection' | null
        'sizeCollection' | ['a']
        'sizeCollection' | (1..5).toList()
        'sizeMap'        | null
        'sizeMap'        | ['a']
        'sizeMap'        | (1..5).collectEntries { it -> [it, it] }
        'matches'        | null
        'matches'        | 'a'
        'matches'        | 'Alessandro'
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
        fieldName        | value                                    || expectedViolations
        'nonNull'        | null                                     || [new ConstraintViolation(null, 'error.validation.not-null', NonNull.DEFAULT_MESSAGE)]
        'assertFalse'    | true                                     || [new ConstraintViolation(true, 'error.validation.required-false', String.format(AssertFalse.DEFAULT_MESSAGE, true))]
        'assertTrue'     | false                                    || [new ConstraintViolation(false, 'error.validation.required-true', String.format(AssertTrue.DEFAULT_MESSAGE, false))]
        'max'            | 1                                        || [new ConstraintViolation(1, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, 1, 0.0))]
        'max'            | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, Integer.MAX_VALUE, 0.0))]
        'negativeOrZero' | 1                                        || [new ConstraintViolation(1, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, 1))]
        'negativeOrZero' | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'negative'       | 0                                        || [new ConstraintViolation(0, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 0))]
        'negative'       | 1                                        || [new ConstraintViolation(1, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 1))]
        'negative'       | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'min'            | -1                                       || [new ConstraintViolation(-1, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, -1, 0.0))]
        'min'            | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, Integer.MIN_VALUE, 0.0))]
        'positiveOrZero' | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, -1))]
        'positiveOrZero' | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'positive'       | 0                                        || [new ConstraintViolation(0, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, 0))]
        'positive'       | -1                                       || [new ConstraintViolation(-1, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, -1))]
        'positive'       | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'range'          | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MIN_VALUE, 10.0, 1.0))]
        'range'          | 0                                        || [new ConstraintViolation(0, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 0, 10.0, 1.0))]
        'range'          | 11                                       || [new ConstraintViolation(11, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 11, 10.0, 1.0))]
        'range'          | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MAX_VALUE, 10.0, 1.0))]
        'port'           | Integer.MIN_VALUE                        || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'port'           | -1                                       || [new ConstraintViolation(-1, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, -1))]
        'port'           | 65536                                    || [new ConstraintViolation(65536, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, 65536))]
        'port'           | Integer.MAX_VALUE                        || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.invalid-port', String.format(Port.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'sizeString'     | ''                                       || [new ConstraintViolation('', 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, '', 5, 1))]
        'sizeString'     | 'a'.repeat(6)                            || [new ConstraintViolation('a'.repeat(6), 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, 'a'.repeat(6), 5, 1))]
        'sizeArray'      | noValuesArray                            || [new ConstraintViolation(noValuesArray, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, noValuesArray, 5, 1))]
        'sizeArray'      | exceedValuesArray                        || [new ConstraintViolation(exceedValuesArray, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, exceedValuesArray, 5, 1))]
        'sizeCollection' | []                                       || [new ConstraintViolation([], 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, [], 5, 1))]
        'sizeCollection' | (1..6).toList()                          || [new ConstraintViolation((1..6).toList(), 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, (1..6).toList(), 5, 1))]
        'sizeMap'        | [:]                                      || [new ConstraintViolation([:], 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, [:], 5, 1))]
        'sizeMap'        | (1..6).collectEntries { it -> [it, it] } ||
                [new ConstraintViolation((1..6).collectEntries { it -> [it, it] }, 'error.validation.argument-exceeds-size', String.format(Size.DEFAULT_MESSAGE, (1..6).collectEntries { it -> [it, it] }, 5, 1))]
        'matches'        | ''                                       || [new ConstraintViolation('', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, '', '[A-Za-z]+'))]
        'matches'        | 'Alessandro!'                            || [new ConstraintViolation('Alessandro!', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, 'Alessandro!', '[A-Za-z]+'))]
        'matches'        | '01001'                                  || [new ConstraintViolation('01001', 'error.validation.invalid-string', String.format(Matches.DEFAULT_MESSAGE, '01001', '[A-Za-z]+'))]
        'minPort'        | 1007                                     || [new ConstraintViolation(1007, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 1007, 100.0, 1.0))]
    }

}
