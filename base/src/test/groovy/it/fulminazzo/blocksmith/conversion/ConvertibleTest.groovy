package it.fulminazzo.blocksmith.conversion

import it.fulminazzo.blocksmith.reflect.ReflectException
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class ConvertibleTest extends Specification {
    private Convertible convertible = new Person('Alex', 23)

    def 'step 1: test that convert without converter throws'() {
        when:
        convertible.as(String)

        then:
        thrown(ReflectException)
    }

    def 'step 2: test that convert with converter returns correct result'() {
        given:
        Convertible.register(Person, String, p -> p.toString())

        when:
        def actual = convertible.as(String)

        then:
        actual == convertible.toString()
    }

    def 'step 3: test that convert can convert to #type'() {
        when:
        def actual = convertible.as(type)

        then:
        actual == convertible

        where:
        type << [Person, Convertible, Object]
    }

}
