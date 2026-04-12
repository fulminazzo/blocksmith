//file:noinspection GrDeprecatedAPIUsage
package it.fulminazzo.blocksmith.command.node.info

import spock.lang.Specification

class CommandInfoTest extends Specification {

    private PermissionInfo permission

    void setup() {
        permission = Mock(PermissionInfo)
    }

    def 'test that merge overwrites empty description'() {
        given:
        def first = new CommandInfo('', permission, autoComputed)

        and:
        def second = new CommandInfo('Goodbye, mars!', permission, true)

        when:
        first.merge(second)

        then:
        first.description == second.description
        first.autoComputed == second.autoComputed

        where:
        autoComputed << [true, false]
    }

    def 'test that merge overwrites auto computed'() {
        given:
        def first = new CommandInfo('Hello, world!', permission, true)

        and:
        def second = new CommandInfo('Goodbye, mars!', permission, false)

        when:
        first.merge(second)

        then:
        first.description == second.description
        first.autoComputed == second.autoComputed
    }

    def 'test that merge does not overwrite non-empty, non-autocomputed command info'() {
        given:
        def first = new CommandInfo('Hello, world!', permission)

        and:
        def second = new CommandInfo('Goodbye, mars!', permission, true)

        when:
        first.merge(second)

        then:
        first.description == 'Hello, world!'
        !first.autoComputed
    }

    def 'test that auto computation does not affect equality'() {
        given:
        def first = new CommandInfo('Hello, world!', permission)

        and:
        def second = new CommandInfo('Hello, world!', permission, true)

        expect:
        first == second

        and:
        first.toString() == second.toString()
    }

}
