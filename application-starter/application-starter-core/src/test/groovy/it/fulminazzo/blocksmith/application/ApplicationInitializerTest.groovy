package it.fulminazzo.blocksmith.application

import spock.lang.Specification

class ApplicationInitializerTest extends Specification {

    def 'test that getFieldValue of #fieldPath returns #expected'() {
        expect:
        ApplicationInitializer.getFieldValue(new ContainerClass(), fieldPath) == expected

        where:
        fieldPath                            || expected
        ''                                   || new ContainerClass()
        'field'                              || new ContainerClass.FieldClass()
        'field.name'                         || 'Alex'
        'field.ageData'                      || new ContainerClass.MethodClass()
        'field.ageData.information'          || new ContainerClass.NestedClass()
        'field.ageData.information.identity' || 'Batman'
        'method'                             || new ContainerClass.MethodClass()
        'method.age'                         || 10
        'method.information'                 || new ContainerClass.NestedClass()
        'method.information.identity'        || 'Batman'
    }

    def 'test that checkFieldInClass throws for #fieldPath'() {
        when:
        ApplicationInitializer.getFieldValue(new ContainerClass(), fieldPath)

        then:
        thrown(IllegalArgumentException)

        where:
        fieldPath << [
                'field.notFound',
                'method.invalid',
                'method.localizedName'
        ]
    }


}
