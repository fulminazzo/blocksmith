package it.fulminazzo.blocksmith.checkstyle.validator

import com.puppycrawl.tools.checkstyle.api.DetailAST
import org.mockito.Mockito
import spock.lang.Specification

@SuppressWarnings('GroovyAccessibility')
class OverloadValidatorTest extends Specification {
    private static final Executable NO_PARAMETERS = new Executable('method', [])
    private static final Executable PRIMITIVE_PARAMETER = new Executable('method', [new Executable.Parameter('int', 'a', true)])
    private static final Executable WRAPPER_PARAMETER = new Executable('method', [new Executable.Parameter('Integer', 'b', false)])

    private final DetailAST noParametersNode = Mock(DetailAST)
    private final DetailAST primitiveParameterNode = Mock(DetailAST)
    private final DetailAST wrapperParameterNode = Mock(DetailAST)

    private final OverloadValidator validator = new OverloadValidator()

    private final executableMock = Mockito.mockStatic(Executable)

    void setup() {
        executableMock.when { Executable.of(noParametersNode) }.thenReturn(NO_PARAMETERS)
        executableMock.when { Executable.of(primitiveParameterNode) }.thenReturn(PRIMITIVE_PARAMETER)
        executableMock.when { Executable.of(wrapperParameterNode) }.thenReturn(WRAPPER_PARAMETER)
    }

    void cleanup() {
        executableMock?.close()
    }

    def 'test that validateNode does not throw and updates last lastExecutable on valid node'() {
        when:
        validator.validateNode(noParametersNode)

        then:
        noExceptionThrown()

        and:
        validator.lastScope.lastExecutable == NO_PARAMETERS

        when:
        validator.validateNode(primitiveParameterNode)

        then:
        noExceptionThrown()

        and:
        validator.lastScope.lastExecutable == PRIMITIVE_PARAMETER

        when:
        validator.validateNode(wrapperParameterNode)

        then:
        noExceptionThrown()

        and:
        validator.lastScope.lastExecutable == WRAPPER_PARAMETER
    }

    def 'test that validateNode when lastExecutable is #lastExecutable throws for node #node'() {
        given:
        validator.lastScope.lastExecutable = OverloadValidatorTest."$lastExecutable"

        when:
        validator.validateNode(this."$node")

        then:
        thrown(ValidationException)

        where:
        lastExecutable        | node
        // primitive parameter
        'PRIMITIVE_PARAMETER' | 'noParametersNode'
        // wrapper parameter
        'WRAPPER_PARAMETER'   | 'noParametersNode'
        'WRAPPER_PARAMETER'   | 'primitiveParameterNode'
    }

    def 'test that exitScope throws if nodes are incorrect'() {
        given:
        def next = Mock(Validator)
        next.validateNode(_) >> { a ->
            throw new ValidationException(a[0], 'mock.exception')
        }

        and:
        validator.then(next)

        when:
        validator.validateNode(noParametersNode)

        then:
        noExceptionThrown()

        when:
        validator.exitScope()

        then:
        def c = thrown(CompositeValidationException)
        c.exceptions.size() == 1

        and:
        def e = c.exceptions[0]
        e.node == noParametersNode
        e.message == 'it.fulminazzo.blocksmith.checkstyle.mock.exception'
    }

}
