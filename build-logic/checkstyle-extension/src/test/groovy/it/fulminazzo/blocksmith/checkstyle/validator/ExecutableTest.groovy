package it.fulminazzo.blocksmith.checkstyle.validator

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import it.fulminazzo.blocksmith.checkstyle.validator.Executable.Parameter
import spock.lang.Specification

@SuppressWarnings('GroovyAccessibility')
class ExecutableTest extends Specification {
    private static final Parameter primitiveParameter = new Parameter('int', 'a', true)
    private static final Parameter wrapperParameter = new Parameter('Integer', 'b', false)

    def 'test that compareTo with #first and #second returns #expected'() {
        expect:
        first <=> second == expected

        where:
        first                                         | second                                        || expected
        // name
        new Executable('print', [])                   | new Executable('write', [])                   || 0
        // less parameters
        new Executable('print', [])                   | new Executable('print', [primitiveParameter]) || -1
        // more parameters
        new Executable('print', [primitiveParameter]) | new Executable('print', [])                   || 1
        // same parameters
        new Executable('print', [primitiveParameter]) | new Executable('print', [primitiveParameter]) || 0
        new Executable('print', [primitiveParameter]) | new Executable('print', [wrapperParameter])   || -1
        new Executable('print', [wrapperParameter])   | new Executable('print', [primitiveParameter]) || 1
        new Executable('print', [wrapperParameter])   | new Executable('print', [wrapperParameter])   || 0
    }

    def 'test that of function works'() {
        given:
        def node = Mock(DetailAST)
        node.type >> type

        and:
        node.findFirstToken(TokenTypes.IDENT) >> {
            def nameNode = Mock(DetailAST)
            nameNode.text >> 'method'
            return nameNode
        }

        and:
        node.findFirstToken(TokenTypes.PARAMETERS) >> {
            def parametersNode = Mock(DetailAST)
            parametersNode.firstChild >> createParameterNode(TokenTypes.LITERAL_INT)
            return parametersNode
        }

        when:
        def executable = Executable.of(node)

        then:
        executable.name == expectedName
        executable.parameters.size() == 1

        and:
        def parameter = executable.parameters[0]
        parameter.type == 'int'
        parameter.name == 'a'
        parameter.primitive

        where:
        type                  || expectedName
        TokenTypes.CTOR_DEF   || '<init>'
        TokenTypes.METHOD_DEF || 'method'
    }

    /*
     * PARAMETER
     */

    def 'test that Parameter compareTo with #first and #second returns #expected'() {
        expect:
        first <=> second == expected

        where:
        first              | second             || expected
        // primitive - primitive
        primitiveParameter | primitiveParameter || 0
        // primitive - wrapper
        primitiveParameter | wrapperParameter   || -1
        // wrapper - primitive
        wrapperParameter   | primitiveParameter || 1
        // wrapper - wrapper
        wrapperParameter   | wrapperParameter   || 0
    }

    def 'test that Parameter of function works'() {
        given:
        def node = createParameterNode(type)

        when:
        def parameter = Parameter.of(node)

        then:
        parameter.type == 'int'
        parameter.name == 'a'
        parameter.primitive == expectedPrimitive

        where:
        [type, expectedPrimitive] << [TokenTypes.LITERAL_BYTE,
                                      TokenTypes.LITERAL_SHORT,
                                      TokenTypes.LITERAL_CHAR,
                                      TokenTypes.LITERAL_INT,
                                      TokenTypes.LITERAL_LONG,
                                      TokenTypes.LITERAL_FLOAT,
                                      TokenTypes.LITERAL_DOUBLE,
                                      TokenTypes.LITERAL_BOOLEAN]
                .collect { [it, true] } + [[TokenTypes.IDENT, false]]
    }

    private DetailAST createParameterNode(final int type) {
        def parameterNode = Mock(DetailAST)
        parameterNode.type >> TokenTypes.PARAMETER_DEF
        parameterNode.findFirstToken(TokenTypes.TYPE) >> {
            def typeNode = Mock(DetailAST)
            typeNode.firstChild >> {
                def actualTypeNode = Mock(DetailAST)
                actualTypeNode.text >> 'int'
                actualTypeNode.type >> type
                return actualTypeNode
            }
            return typeNode
        }
        parameterNode.findFirstToken(TokenTypes.IDENT) >> {
            def nameNode = Mock(DetailAST)
            nameNode.text >> 'a'
            return nameNode
        }
        return parameterNode
    }

}
