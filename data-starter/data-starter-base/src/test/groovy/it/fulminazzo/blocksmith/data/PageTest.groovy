package it.fulminazzo.blocksmith.data

import spock.lang.Specification

class PageTest extends Specification {

    def 'test that first works'() {
        given:
        def page = new Page(10, 10)

        when:
        def first = page.first()

        then:
        first.number == 0
        first.size == page.size
    }

    def 'test that next works'() {
        given:
        def page = new Page(current, 10)

        when:
        def next = page.next()

        then:
        next.number == expected
        next.size == page.size

        where:
        current || expected
        10      || 11
        2       || 3
        1       || 2
        0       || 1
    }

    def 'test that previous works'() {
        given:
        def page = new Page(current, 10)

        when:
        def previous = page.previous()

        then:
        previous.number == expected
        previous.size == page.size

        where:
        current || expected
        10      || 9
        2       || 1
        1       || 0
        0       || 0
    }

}
