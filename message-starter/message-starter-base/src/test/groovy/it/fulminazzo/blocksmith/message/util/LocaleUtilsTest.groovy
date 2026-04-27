package it.fulminazzo.blocksmith.message.util

import spock.lang.Specification

class LocaleUtilsTest extends Specification {

    def 'test locale conversion'() {
        given:
        def expected = Locale.ITALY

        when:
        def string = LocaleUtils.toString(expected)

        then:
        string == 'it_it'

        when:
        def actual = LocaleUtils.fromString(string)

        then:
        actual == expected
    }

}
