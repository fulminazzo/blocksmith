//file:noinspection unused
package it.fulminazzo.blocksmith.validation

import it.fulminazzo.blocksmith.validation.annotation.*
import spock.lang.Specification

class ValidatorTest extends Specification {
    private static final Validator validator = Validator.instance

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
        fieldName        | value             || expectedViolations
        'nonNull'        | null              || [new ConstraintViolation(null, 'error.validation.not-null', NonNull.DEFAULT_MESSAGE)]
        'assertFalse'    | true              || [new ConstraintViolation(true, 'error.validation.required-false', String.format(AssertFalse.DEFAULT_MESSAGE, true))]
        'assertTrue'     | false             || [new ConstraintViolation(false, 'error.validation.required-true', String.format(AssertTrue.DEFAULT_MESSAGE, false))]
        'max'            | 1                 || [new ConstraintViolation(1, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, 1, 0.0))]
        'max'            | Integer.MAX_VALUE || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-too-big', String.format(Max.DEFAULT_MESSAGE, Integer.MAX_VALUE, 0.0))]
        'negativeOrZero' | 1                 || [new ConstraintViolation(1, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, 1))]
        'negativeOrZero' | Integer.MAX_VALUE || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative-or-zero', String.format(NegativeOrZero.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'negative'       | 0                 || [new ConstraintViolation(0, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 0))]
        'negative'       | 1                 || [new ConstraintViolation(1, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, 1))]
        'negative'       | Integer.MAX_VALUE || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.negative', String.format(Negative.DEFAULT_MESSAGE, Integer.MAX_VALUE))]
        'min'            | -1                || [new ConstraintViolation(-1, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, -1, 0.0))]
        'min'            | Integer.MIN_VALUE || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-too-small', String.format(Min.DEFAULT_MESSAGE, Integer.MIN_VALUE, 0.0))]
        'positiveOrZero' | -1                || [new ConstraintViolation(-1, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, -1))]
        'positiveOrZero' | Integer.MIN_VALUE || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive-or-zero', String.format(PositiveOrZero.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'positive'       | 0                 || [new ConstraintViolation(0, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, 0))]
        'positive'       | -1                || [new ConstraintViolation(-1, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, -1))]
        'positive'       | Integer.MIN_VALUE || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.positive', String.format(Positive.DEFAULT_MESSAGE, Integer.MIN_VALUE))]
        'range'          | Integer.MIN_VALUE || [new ConstraintViolation(Integer.MIN_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MIN_VALUE, 1, 10))]
        'range'          | 0                 || [new ConstraintViolation(0, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 0, 1, 10))]
        'range'          | 11                || [new ConstraintViolation(11, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, 11, 1, 10))]
        'range'          | Integer.MAX_VALUE || [new ConstraintViolation(Integer.MAX_VALUE, 'error.validation.number-exceeds-range', String.format(Range.DEFAULT_MESSAGE, Integer.MAX_VALUE, 1, 10))]
    }

}
