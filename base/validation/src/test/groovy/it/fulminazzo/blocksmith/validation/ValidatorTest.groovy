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

    def 'test that validate of field #fieldName and value #value does not throw'() {
        given:
        def field = ValidatorTest.getDeclaredField(fieldName)

        when:
        validator.validate(field, value)

        then:
        noExceptionThrown()

        where:
        fieldName     | value
        'nonNull'     | new Object()
        'assertFalse' | false
        'assertFalse' | null
        'assertTrue'  | true
        'assertTrue'  | null
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
        fieldName     | value || expectedViolations
        'nonNull'     | null  || [new ConstraintViolation(null, 'error.validation.not-null', NonNull.DEFAULT_MESSAGE)]
        'assertFalse' | true  || [new ConstraintViolation(true, 'error.validation.required-false', AssertFalse.DEFAULT_MESSAGE)]
        'assertTrue'  | false || [new ConstraintViolation(false, 'error.validation.required-true', AssertTrue.DEFAULT_MESSAGE)]
    }

}
