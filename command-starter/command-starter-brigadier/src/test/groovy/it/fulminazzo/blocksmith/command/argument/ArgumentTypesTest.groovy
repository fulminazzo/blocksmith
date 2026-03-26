package it.fulminazzo.blocksmith.command.argument

import com.mojang.brigadier.arguments.*
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import spock.lang.Specification

class ArgumentTypesTest extends Specification {

    def 'test that of of #node returns #expected'() {
        when:
        def actual = ArgumentTypes.of(node as ArgumentNode)

        then:
        actual.class == expected

        where:
        node                                                           || expected
        ArgumentNode.newNode('number', Byte, false)                    || IntegerArgumentType
        ArgumentNode.newNode('number', Short, false)                   || IntegerArgumentType
        ArgumentNode.newNode('number', Integer, false)                 || IntegerArgumentType
        ArgumentNode.newNode('number', Long, false)                    || LongArgumentType
        ArgumentNode.newNode('number', Float, false)                   || FloatArgumentType
        ArgumentNode.newNode('number', Double, false)                  || DoubleArgumentType
        ArgumentNode.newNode('boolean', Boolean, false)                || BoolArgumentType
        ArgumentNode.newNode('string', String, false)                  || StringArgumentType
        ArgumentNode.newNode('number', Integer, false).setGreedy(true) || StringArgumentType
        ArgumentNode.newNode('string', String, false).setGreedy(true)  || StringArgumentType
    }

}
