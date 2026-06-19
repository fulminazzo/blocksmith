package it.fulminazzo.blocksmith.application

import spock.lang.Specification

import java.lang.annotation.Documented

class ApplicationHandlersTest extends Specification {

    def 'test that register works'() {
        given:
        def annotation = Documented
        def handler = {} as FieldAnnotationHandler

        and:
        ApplicationHandlers.registerFieldAnnotationHandler(annotation, handler)

        when:
        def actual = ApplicationHandlers.getFieldAnnotationHandler(annotation)

        then:
        actual.present
        actual.get() == handler
    }

}
