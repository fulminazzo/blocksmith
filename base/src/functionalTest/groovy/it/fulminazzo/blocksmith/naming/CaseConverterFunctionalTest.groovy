package it.fulminazzo.blocksmith.naming

import spock.lang.Specification

class CaseConverterFunctionalTest extends Specification {

    def 'test that convert #input to #to returns #expected'() {
        when:
        def actual = CaseConverter.convert(input, to)

        then:
        actual == expected

        where:
        input        | to                     || expected
        // camelCase
        'helloWorld' | Convention.CAMEL_CASE  || 'helloWorld'
        'hW'         | Convention.CAMEL_CASE  || 'hW'
        ''           | Convention.CAMEL_CASE  || ''
        // kebab-case
        'helloWorld' | Convention.KEBAB_CASE  || 'hello-world'
        'hW'         | Convention.KEBAB_CASE  || 'h-w'
        ''           | Convention.KEBAB_CASE  || ''
        // snake_case
        'helloWorld' | Convention.SNAKE_CASE  || 'hello_world'
        'hW'         | Convention.SNAKE_CASE  || 'h_w'
        ''           | Convention.SNAKE_CASE  || ''
        // PascalCase
        'helloWorld' | Convention.PASCAL_CASE || 'HelloWorld'
        'hW'         | Convention.PASCAL_CASE || 'HW'
        ''           | Convention.PASCAL_CASE || ''
    }

    def 'test that convert #input from #from to #to returns #expected'() {
        when:
        def actual = CaseConverter.convert(input, from, to)

        then:
        actual == expected

        where:
        input         | from                   | to                     || expected
        // camelCase
        'helloWorld'  | Convention.CAMEL_CASE  | Convention.CAMEL_CASE  || 'helloWorld'
        'hello-world' | Convention.KEBAB_CASE  | Convention.CAMEL_CASE  || 'helloWorld'
        'hello_world' | Convention.SNAKE_CASE  | Convention.CAMEL_CASE  || 'helloWorld'
        'HelloWorld'  | Convention.PASCAL_CASE | Convention.CAMEL_CASE  || 'helloWorld'
        'hW'          | Convention.CAMEL_CASE  | Convention.CAMEL_CASE  || 'hW'
        'h-w'         | Convention.KEBAB_CASE  | Convention.CAMEL_CASE  || 'hW'
        'h_w'         | Convention.SNAKE_CASE  | Convention.CAMEL_CASE  || 'hW'
        'HW'          | Convention.PASCAL_CASE | Convention.CAMEL_CASE  || 'hW'
        ''            | Convention.CAMEL_CASE  | Convention.CAMEL_CASE  || ''
        ''            | Convention.KEBAB_CASE  | Convention.CAMEL_CASE  || ''
        ''            | Convention.SNAKE_CASE  | Convention.CAMEL_CASE  || ''
        ''            | Convention.PASCAL_CASE | Convention.CAMEL_CASE  || ''
        // kebab-case
        'helloWorld'  | Convention.CAMEL_CASE  | Convention.KEBAB_CASE  || 'hello-world'
        'hello-world' | Convention.KEBAB_CASE  | Convention.KEBAB_CASE  || 'hello-world'
        'hello_world' | Convention.SNAKE_CASE  | Convention.KEBAB_CASE  || 'hello-world'
        'HelloWorld'  | Convention.PASCAL_CASE | Convention.KEBAB_CASE  || 'hello-world'
        'hW'          | Convention.CAMEL_CASE  | Convention.KEBAB_CASE  || 'h-w'
        'h-w'         | Convention.KEBAB_CASE  | Convention.KEBAB_CASE  || 'h-w'
        'h_w'         | Convention.SNAKE_CASE  | Convention.KEBAB_CASE  || 'h-w'
        'HW'          | Convention.PASCAL_CASE | Convention.KEBAB_CASE  || 'h-w'
        ''            | Convention.CAMEL_CASE  | Convention.KEBAB_CASE  || ''
        ''            | Convention.KEBAB_CASE  | Convention.KEBAB_CASE  || ''
        ''            | Convention.SNAKE_CASE  | Convention.KEBAB_CASE  || ''
        ''            | Convention.PASCAL_CASE | Convention.KEBAB_CASE  || ''
        // snake_case
        'helloWorld'  | Convention.CAMEL_CASE  | Convention.SNAKE_CASE  || 'hello_world'
        'hello-world' | Convention.KEBAB_CASE  | Convention.SNAKE_CASE  || 'hello_world'
        'hello_world' | Convention.SNAKE_CASE  | Convention.SNAKE_CASE  || 'hello_world'
        'HelloWorld'  | Convention.PASCAL_CASE | Convention.SNAKE_CASE  || 'hello_world'
        'hW'          | Convention.CAMEL_CASE  | Convention.SNAKE_CASE  || 'h_w'
        'h-w'         | Convention.KEBAB_CASE  | Convention.SNAKE_CASE  || 'h_w'
        'h_w'         | Convention.SNAKE_CASE  | Convention.SNAKE_CASE  || 'h_w'
        'HW'          | Convention.PASCAL_CASE | Convention.SNAKE_CASE  || 'h_w'
        ''            | Convention.CAMEL_CASE  | Convention.SNAKE_CASE  || ''
        ''            | Convention.KEBAB_CASE  | Convention.SNAKE_CASE  || ''
        ''            | Convention.SNAKE_CASE  | Convention.SNAKE_CASE  || ''
        ''            | Convention.PASCAL_CASE | Convention.SNAKE_CASE  || ''
        // PascalCase
        'helloWorld'  | Convention.CAMEL_CASE  | Convention.PASCAL_CASE || 'HelloWorld'
        'hello-world' | Convention.KEBAB_CASE  | Convention.PASCAL_CASE || 'HelloWorld'
        'hello_world' | Convention.SNAKE_CASE  | Convention.PASCAL_CASE || 'HelloWorld'
        'HelloWorld'  | Convention.PASCAL_CASE | Convention.PASCAL_CASE || 'HelloWorld'
        'hW'          | Convention.CAMEL_CASE  | Convention.PASCAL_CASE || 'HW'
        'h-w'         | Convention.KEBAB_CASE  | Convention.PASCAL_CASE || 'HW'
        'h_w'         | Convention.SNAKE_CASE  | Convention.PASCAL_CASE || 'HW'
        'HW'          | Convention.PASCAL_CASE | Convention.PASCAL_CASE || 'HW'
        ''            | Convention.CAMEL_CASE  | Convention.PASCAL_CASE || ''
        ''            | Convention.KEBAB_CASE  | Convention.PASCAL_CASE || ''
        ''            | Convention.SNAKE_CASE  | Convention.PASCAL_CASE || ''
        ''            | Convention.PASCAL_CASE | Convention.PASCAL_CASE || ''
    }

}
