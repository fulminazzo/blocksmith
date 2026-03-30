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
    }

}
