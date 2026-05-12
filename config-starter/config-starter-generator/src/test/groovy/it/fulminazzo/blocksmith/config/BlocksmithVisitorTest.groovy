package it.fulminazzo.blocksmith.config

import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.expr.ArrayInitializerExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LiteralExpr
import com.github.javaparser.printer.SourcePrinter
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import it.fulminazzo.blocksmith.config.BeanConfigurationBuilder.BlocksmithVisitor
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class BlocksmithVisitorTest extends Specification {
    private final BlocksmithVisitor validator = Spy(BlocksmithVisitor, constructorArgs : [new DefaultPrinterConfiguration()])
    private final SourcePrinter printer = Mock(SourcePrinter)

    void setup() {
        Reflect.on(validator).set('printer', printer)
    }

    def 'test that visit of array with non-string arrays does not indent'() {
        given:
        def expression = new ArrayInitializerExpr(new NodeList<>([
                new IntegerLiteralExpr('1'),
                new IntegerLiteralExpr('2'),
                new IntegerLiteralExpr('3')
        ]))

        when:
        validator.visit(expression, null)

        then:
        noExceptionThrown()

        and:
        1 * printer.print('{')
        2 * printer.print(' ')
        2 * printer.print(', ')
        1 * printer.print('}')
    }

    def 'test that visit of array does not throw if empty'() {
        given:
        def expression = new ArrayInitializerExpr(new NodeList<>())

        when:
        validator.visit(expression, null)

        then:
        noExceptionThrown()

        and:
        1 * printer.print('{')
        1 * printer.print('}')
    }

}
