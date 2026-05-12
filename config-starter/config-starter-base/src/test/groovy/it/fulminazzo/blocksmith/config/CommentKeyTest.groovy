package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class CommentKeyTest extends Specification {

    def 'test that compareTo of #first and #second returns expected'() {
        given:
        def k1 = new CommentKey(first)
        def k2 = new CommentKey(second)

        expect:
        (k1 <=> k2) == (first <=> second)

        where:
        first   | second
        'Hello' | 'Hello'
        'Hello' | 'world'
        'world' | 'Hello'
        'world' | 'world'
    }

}
