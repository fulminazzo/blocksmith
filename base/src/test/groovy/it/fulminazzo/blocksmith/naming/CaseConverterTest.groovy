package it.fulminazzo.blocksmith.naming

import spock.lang.Specification

import static it.fulminazzo.blocksmith.naming.Convention.*

class CaseConverterTest extends Specification {

    def 'test that convert #input from #from to #to returns #expected'() {
        when:
        def actual = CaseConverter.convert(input, from, to)

        then:
        actual == expected

        where:
        input         | from        | to          || expected
        // camelCase
        'helloWorld'  | CAMEL_CASE  | CAMEL_CASE  || 'helloWorld'
        'hello-world' | KEBAB_CASE  | CAMEL_CASE  || 'helloWorld'
        'hello_world' | SNAKE_CASE  | CAMEL_CASE  || 'helloWorld'
        'HelloWorld'  | PASCAL_CASE | CAMEL_CASE  || 'helloWorld'
        'hW'          | CAMEL_CASE  | CAMEL_CASE  || 'hW'
        'h-w'         | KEBAB_CASE  | CAMEL_CASE  || 'hW'
        'h_w'         | SNAKE_CASE  | CAMEL_CASE  || 'hW'
        'HW'          | PASCAL_CASE | CAMEL_CASE  || 'hW'
        ''            | CAMEL_CASE  | CAMEL_CASE  || ''
        ''            | KEBAB_CASE  | CAMEL_CASE  || ''
        ''            | SNAKE_CASE  | CAMEL_CASE  || ''
        ''            | PASCAL_CASE | CAMEL_CASE  || ''
        // kebab-case
        'helloWorld'  | CAMEL_CASE  | KEBAB_CASE  || 'hello-world'
        'hello-world' | KEBAB_CASE  | KEBAB_CASE  || 'hello-world'
        'hello_world' | SNAKE_CASE  | KEBAB_CASE  || 'hello-world'
        'HelloWorld'  | PASCAL_CASE | KEBAB_CASE  || 'hello-world'
        'hW'          | CAMEL_CASE  | KEBAB_CASE  || 'h-w'
        'h-w'         | KEBAB_CASE  | KEBAB_CASE  || 'h-w'
        'h_w'         | SNAKE_CASE  | KEBAB_CASE  || 'h-w'
        'HW'          | PASCAL_CASE | KEBAB_CASE  || 'h-w'
        ''            | CAMEL_CASE  | KEBAB_CASE  || ''
        ''            | KEBAB_CASE  | KEBAB_CASE  || ''
        ''            | SNAKE_CASE  | KEBAB_CASE  || ''
        ''            | PASCAL_CASE | KEBAB_CASE  || ''
        // snake_case
        'helloWorld'  | CAMEL_CASE  | SNAKE_CASE  || 'hello_world'
        'hello-world' | KEBAB_CASE  | SNAKE_CASE  || 'hello_world'
        'hello_world' | SNAKE_CASE  | SNAKE_CASE  || 'hello_world'
        'HelloWorld'  | PASCAL_CASE | SNAKE_CASE  || 'hello_world'
        'hW'          | CAMEL_CASE  | SNAKE_CASE  || 'h_w'
        'h-w'         | KEBAB_CASE  | SNAKE_CASE  || 'h_w'
        'h_w'         | SNAKE_CASE  | SNAKE_CASE  || 'h_w'
        'HW'          | PASCAL_CASE | SNAKE_CASE  || 'h_w'
        ''            | CAMEL_CASE  | SNAKE_CASE  || ''
        ''            | KEBAB_CASE  | SNAKE_CASE  || ''
        ''            | SNAKE_CASE  | SNAKE_CASE  || ''
        ''            | PASCAL_CASE | SNAKE_CASE  || ''
        // PascalCase
        'helloWorld'  | CAMEL_CASE  | PASCAL_CASE || 'HelloWorld'
        'hello-world' | KEBAB_CASE  | PASCAL_CASE || 'HelloWorld'
        'hello_world' | SNAKE_CASE  | PASCAL_CASE || 'HelloWorld'
        'HelloWorld'  | PASCAL_CASE | PASCAL_CASE || 'HelloWorld'
        'hW'          | CAMEL_CASE  | PASCAL_CASE || 'HW'
        'h-w'         | KEBAB_CASE  | PASCAL_CASE || 'HW'
        'h_w'         | SNAKE_CASE  | PASCAL_CASE || 'HW'
        'HW'          | PASCAL_CASE | PASCAL_CASE || 'HW'
        ''            | CAMEL_CASE  | PASCAL_CASE || ''
        ''            | KEBAB_CASE  | PASCAL_CASE || ''
        ''            | SNAKE_CASE  | PASCAL_CASE || ''
        ''            | PASCAL_CASE | PASCAL_CASE || ''
    }

}
