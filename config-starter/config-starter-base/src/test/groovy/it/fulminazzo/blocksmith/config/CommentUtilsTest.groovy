package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class CommentUtilsTest extends Specification {

    def 'test that isEmpty of #comment returns #expected'() {
        expect:
        CommentUtils.isEmpty(newComment(comment)) == expected

        where:
        comment         || expected
        []              || true
        ['']            || true
        ['', '']        || true
        ['', '', '']    || true
        [' ', '', '']   || true
        ['', ' ', '']   || true
        ['', '', ' ']   || true
        [' ', ' ', ' '] || true
        ['a', '', '']   || false
        ['', 'a', '']   || false
        ['', '', 'a']   || false
        ['a', 'a', 'a'] || false
    }

    def 'test that getText of comment returns correctly formatted text'() {
        given:
        def annotationComment = newComment(comment)

        when:
        def actual = CommentUtils.getText(annotationComment)

        then:
        actual == expected

        where:
        expected           || comment
        ['Hello', 'world'] || ['Hello', 'world']
        ['Hello', 'world'] || ['Hello\nworld']
        ['Hello', 'world'] || ["""Hello
world"""]
    }

    protected Comment newComment(final List<String> commentLines) {
        def comment = Mock(Comment)
        comment.value() >> commentLines.toArray(new String[0])
        return comment
    }

}
