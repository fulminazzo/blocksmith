package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def 'test that split #string, #regex, #quoted and #quotes returns #expected'() {
        when:
        def actual = StringUtils.split(string, regex, quoted, *quotes)

        then:
        actual == expected

        where:
        string                          | regex  | quoted | quotes     || expected
        'a,b,c'                         | ','    | true   | []         || ['a', 'b', 'c']
        'a,b,c'                         | ','    | false  | []         || ['a', 'b', 'c']
        'hello world'                   | ' '    | true   | []         || ['hello', 'world']
        'hello world'                   | ' '    | false  | []         || ['hello', 'world']
        'one'                           | ','    | true   | []         || ['one']
        'one'                           | ','    | false  | []         || ['one']
        ''                              | ','    | true   | []         || ['']
        ''                              | ','    | false  | []         || ['']
        'a,,b'                          | ','    | true   | []         || ['a', '', 'b']
        'a,,b'                          | ','    | false  | []         || ['a', '', 'b']

        'a1b2c'                         | '\\d'  | true   | []         || ['a', 'b', 'c']
        'a1b2c'                         | '\\d'  | false  | []         || ['a', 'b', 'c']
        'a1b2c3'                        | '\\d'  | true   | []         || ['a', 'b', 'c', '']
        'a1b2c3'                        | '\\d'  | false  | []         || ['a', 'b', 'c', '']
        'one  two'                      | '\\s+' | true   | []         || ['one', 'two']
        'one  two'                      | '\\s+' | false  | []         || ['one', 'two']

        '"a,b",c'                       | ','    | true   | ['"']      || ['"a,b"', 'c']
        '"a,b",c'                       | ','    | false  | ['"']      || ['a,b', 'c']
        '"a,b",c"'                      | ','    | true   | ['"']      || ['"a,b"', 'c"']
        '"a,b",c"'                      | ','    | false  | ['"']      || ['a,b', 'c"']
        "'a,b',c"                       | ','    | true   | ["'"]      || ["'a,b'", 'c']
        "'a,b',c"                       | ','    | false  | ["'"]      || ["a,b", 'c']
        '"a,b","c,d"'                   | ','    | true   | ['"']      || ['"a,b"', '"c,d"']
        '"a,b","c,d"'                   | ','    | false  | ['"']      || ['a,b', 'c,d']
        '"a,b","c,d'                    | ','    | true   | ['"']      || ['"a,b"', '"c,d']
        '"a,b","c,d'                    | ','    | false  | ['"']      || ['a,b', '"c,d']

        '"a,b",\'c,d\',e'               | ','    | true   | ['"', "'"] || ['"a,b"', "'c,d'", 'e']
        '"a,b",\'c,d\',e'               | ','    | false  | ['"', "'"] || ['a,b', "c,d", 'e']

        'first "and \"second\"" third' || ' '    | true   | ['"']      || ['first', '"and \"second\""', 'third']
        'first "and \"second\"" third' || ' '    | false  | ['"']      || ['first', 'and \"second\"', 'third']

        '"a,b,c'                        | ','    | true   | ['"']      || ['"a,b,c']
        '"a,b,c'                        | ','    | false  | ['"']      || ['"a,b,c']

        'nodivision'                    | ','    | true   | ['"']      || ['nodivision']
        'nodivision'                    | ','    | false  | ['"']      || ['nodivision']

        'a,b,c'                         | ','    | true   | []         || ['a', 'b', 'c']
        'a,b,c'                         | ','    | false  | []         || ['a', 'b', 'c']

        ',a,b'                          | ','    | true   | []         || ['', 'a', 'b']
        ',a,b'                          | ','    | false  | []         || ['', 'a', 'b']
        'a,b,'                          | ','    | true   | []         || ['a', 'b', '']
        'a,b,'                          | ','    | false  | []         || ['a', 'b', '']
    }

}
