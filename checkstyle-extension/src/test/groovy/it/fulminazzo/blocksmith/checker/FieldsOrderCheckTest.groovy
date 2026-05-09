package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.FileContents
import com.puppycrawl.tools.checkstyle.api.FileText
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class FieldsOrderCheckTest extends Specification {
    private NodeValidator validator
    private FieldsOrderCheck check

    private DetailAST node = Mock(DetailAST)

    void setup() {
        check = Mock(FieldsOrderCheck)
        check.setFileContents(new FileContents(new FileText(
                File.createTempFile('TestClass', 'java'),
                ['class TestClass {}']
        )))
        check.visitToken(_) >> {
            callRealMethod()
        }
        check.leaveToken(_) >> {
            callRealMethod()
        }

        def reflect = Reflect.on(check)
        validator = Mock(NodeValidator)
        reflect.set('validator', validator)

        def parent = Mock(DetailAST)
        node.parent >> parent
    }

    def 'test that visitToken enters scope on #tokenType'() {
        given:
        node.type >> tokenType

        when:
        check.visitToken(node)

        then:
        0 * validator.validateNode(_)
        1 * validator.enterScope()

        where:
        tokenType << [TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF]
    }

    def 'test that visitToken does not throw on ValidationException'() {
        given:
        node.parent.type >> TokenTypes.OBJBLOCK
        node.type >> TokenTypes.VARIABLE_DEF
        node.lineNo >> 1
        node.columnNo >> 2

        and:
        validator.validateNode(_) >> {
            throw new ValidationException('exception.message')
        }

        when:
        check.visitToken(node)

        then:
        noExceptionThrown()

        and:
        1 * check.log(node, "${FieldsOrderCheckTest.packageName}.exception.message")
    }

    def 'test that visitToken works only if parent is OBJBLOCK'() {
        when:
        node.parent.type >> TokenTypes.TEXT_BLOCK_CONTENT
        node.type >> TokenTypes.VARIABLE_DEF

        and:
        check.visitToken(node)

        then:
        0 * validator.validateNode(node)

        when:
        node.parent.type >> TokenTypes.OBJBLOCK
        node.type >> TokenTypes.VARIABLE_DEF

        and:
        check.visitToken(node)

        then:
        1 * validator.validateNode(node)
    }

    def 'test that leaveToken exits scope on #tokenType'() {
        given:
        node.type >> tokenType

        when:
        check.leaveToken(node)

        then:
        1 * validator.exitScope()

        where:
        tokenType << [TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.ENUM_DEF, TokenTypes.RECORD_DEF]
    }

}
