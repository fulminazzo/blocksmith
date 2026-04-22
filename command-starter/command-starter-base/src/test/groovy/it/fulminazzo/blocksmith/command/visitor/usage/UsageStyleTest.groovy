package it.fulminazzo.blocksmith.command.visitor.usage

import spock.lang.Specification

class UsageStyleTest extends Specification {

    def 'test that getters and setters of #field return #defaultExpected and #expected and default resets to default values'() {
        when:
        def style = UsageStyle.get()

        then:
        style != null

        when:
        def actual = style."$field"

        then:
        actual == defaultExpected

        when:
        style."$field"(value)

        and:
        actual = style."$field"

        then:
        actual == expected

        when:
        style = style.defaults()

        and:
        actual = style."$field"

        then:
        actual == defaultExpected

        when:
        style = UsageStyle.get()

        and:
        actual = style."$field"

        then:
        actual == defaultExpected

        where:
        field                          | value             || defaultExpected || expected
        // COMMON
        'separator'                    | 'SEP'             || '|'             || 'SEP'
        'punctuationColor'             | 'PUN_COL'         || 'dark_gray'     || 'PUN_COL'
        // LITERAL
        'literalColor'                 | 'LIT_COL'         || 'red'           || 'LIT_COL'
        'literalSeparator'             | 'LIT_SEP'         || '|'             || 'LIT_SEP'
        'literalSeparatorColor'        | 'LIT_SEP_COL'     || 'dark_gray'     || 'LIT_SEP_COL'
        // MANDATORY ARGUMENT
        'defaultArgumentColor'         | 'ARG_COL'         || 'yellow'        || 'ARG_COL'
        'argumentFormat'               | 'ARG_FORM'        ||
                '<dark_gray><</dark_gray>%s<dark_gray>></dark_gray>'          || 'ARG_FORM'
        // OPTIONAL ARGUMENT
        'defaultOptionalArgumentColor' | 'OPT_ARG_COL'     || 'aqua'          || 'OPT_ARG_COL'
        'optionalArgumentFormat'       | 'OPT_ARG_FORM'    ||
                '<dark_gray>[</dark_gray>%s<dark_gray>]</dark_gray>'          || 'OPT_ARG_FORM'
        // ARGUMENT
        'greedyArgumentFormat'         | 'GREEDY_ARG_FORM' || '%s...'         || 'GREEDY_ARG_FORM'
    }

    def 'test that #methodName returns #expectedDefault and #expected'() {
        given:
        def style = UsageStyle.get()

        when:
        def actual = style."get$methodName"(UsageStyleTest)

        then:
        actual == expectedDefault

        when:
        style.setArgumentColor(UsageStyleTest, 'COL')

        and:
        actual = style."get$methodName"(UsageStyleTest)

        then:
        actual == expected

        when:
        style.setArgumentColor(UsageStyleTest, null)

        and:
        actual = style."get$methodName"(UsageStyleTest)

        then:
        actual == expectedDefault

        where:
        methodName              || expectedDefault || expected
        'ArgumentColor'         || 'yellow'        || 'COL'
        'OptionalArgumentColor' || 'aqua'          || 'COL'
    }

}
