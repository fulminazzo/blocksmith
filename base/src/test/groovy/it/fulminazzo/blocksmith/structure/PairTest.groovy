package it.fulminazzo.blocksmith.structure

import spock.lang.Specification

class PairTest extends Specification {

    def 'test that map applies the function to both elements'() {
        given:
        def pair = Pair.of('hello', 'world')

        when:
        def result = pair.map { f, s -> "$f $s" }

        then:
        result == 'hello world'
    }

    def 'test that mapFirst returns a new pair with the first element transformed'() {
        given:
        def pair = Pair.of('hello', 42)

        when:
        def result = pair.mapFirst { it.toUpperCase() }

        then:
        result.first == 'HELLO'
        result.second == 42
    }

    def 'test that mapSecond returns a new pair with the second element transformed'() {
        given:
        def pair = Pair.of('hello', 42)

        when:
        def result = pair.mapSecond { it * 2 }

        then:
        result.first == 'hello'
        result.second == 84
    }

    def 'test that swap returns a new pair with first and second inverted'() {
        given:
        def pair = Pair.of('hello', 42)

        when:
        def result = pair.swap()

        then:
        result.first == 42
        result.second == 'hello'
    }

    def 'test that compareTo of #first and #second returns #expected'() {
        expect:
        first.compareTo(second) == expected
        second.compareTo(first) == -expected

        where:
        first                  | second               || expected
        Pair.of('hello', 1)    | Pair.of('hello', 1)  || 0
        Pair.of('apple', 1)    | Pair.of('banana', 1) || -1
        Pair.of('hello', 1)    | Pair.of('hello', 2)  || -1
        Pair.of(null, 1)       | Pair.of('hello', 1)  || 1
        Pair.of(null, 1)       | Pair.of(null, 1)     || 0
        Pair.of('hello', null) | Pair.of('hello', 1)  || 1
        Pair.of(null, null)    | Pair.of(null, null)  || 0
    }

    def 'test that compareTo throws IllegalArgumentException for #first and #second'() {
        when:
        first.compareTo(second)

        then:
        thrown(IllegalArgumentException)

        where:
        first                          | second
        Pair.of(new Object(), 1)       | Pair.of(new Object(), 1)
        Pair.of('hello', new Object()) | Pair.of('hello', new Object())
    }

}