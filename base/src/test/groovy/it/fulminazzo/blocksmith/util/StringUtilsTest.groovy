package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def 'test that split #string, #regex, #quotes returns #expected'() {
        when:
        def actual = StringUtils.split(string, regex, *quotes)

        then:
        actual == expected

        where:
        string            | regex  | quotes     || expected
        'a,b,c'           | ','    | []         || ['a', 'b', 'c']
        'hello world'     | ' '    | []         || ['hello', 'world']
        'one'             | ','    | []         || ['one']
        ''                | ','    | []         || ['']
        'a,,b'            | ','    | []         || ['a', '', 'b']

        'a1b2c'           | '\\d'  | []         || ['a', 'b', 'c']
        'a1b2c3'           | '\\d'  | []         || ['a', 'b', 'c', '']
        'one  two'        | '\\s+' | []         || ['one', 'two']

        '"a,b",c'         | ','    | ['"']      || ['"a,b"', 'c']
        "'a,b',c"         | ','    | ["'"]      || ["'a,b'", 'c']
        '"a,b","c,d"'     | ','    | ['"']      || ['"a,b"', '"c,d"']

        '"a,b",\'c,d\',e' | ','    | ['"', "'"] || ['"a,b"', "'c,d'", 'e']

        '"a,b,c'          | ','    | ['"']      || ['"a', 'b', 'c']

        'nodivision'      | ','    | ['"']      || ['nodivision']

        'a,b,c'           | ','    | []         || ['a', 'b', 'c']

        ',a,b'            | ','    | []         || ['', 'a', 'b']
        'a,b,'            | ','    | []         || ['a', 'b', '']
    }

}
