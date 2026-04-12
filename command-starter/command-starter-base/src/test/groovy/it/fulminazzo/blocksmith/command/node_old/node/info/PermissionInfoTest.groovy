//file:noinspection GrDeprecatedAPIUsage
package it.fulminazzo.blocksmith.command.node_old.node.info

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import spock.lang.Specification

class PermissionInfoTest extends Specification {

    def 'test that merge overwrites empty permission'() {
        given:
        def first = new PermissionInfo(null, '', Permission.Grant.OP, autoComputed)

        and:
        def second = new PermissionInfo('blocksmith', 'command.permission', Permission.Grant.ALL, true)

        when:
        first.merge(second)

        then:
        first.prefix == second.prefix
        first.actualPermission == second.actualPermission
        first.autoComputed == second.autoComputed
        first.grant == second.grant

        where:
        autoComputed << [true, false]
    }

    def 'test that merge overwrites auto computed'() {
        given:
        def first = new PermissionInfo('blk', 'permission', Permission.Grant.ALL, true)

        and:
        def second = new PermissionInfo('blocksmith', 'command.permission', Permission.Grant.OP, false)

        when:
        first.merge(second)

        then:
        first.prefix == second.prefix
        first.actualPermission == second.actualPermission
        first.autoComputed == second.autoComputed
        first.grant == Permission.Grant.ALL
    }

    def 'test that merge does not overwrite non-empty, non-autocomputed permission'() {
        given:
        def first = new PermissionInfo(null, 'permission', Permission.Grant.OP)

        and:
        def second = new PermissionInfo('blocksmith', 'command.permission', Permission.Grant.ALL, true)

        when:
        first.merge(second)

        then:
        first.prefix == null
        first.actualPermission == 'permission'
        !first.autoComputed
        first.grant == second.grant
    }

    def 'test that auto computation does not affect equality'() {
        given:
        def first = new PermissionInfo('blocksmith', 'permission', Permission.Grant.ALL)

        and:
        def second = new PermissionInfo('blocksmith', 'permission', Permission.Grant.ALL, true)

        expect:
        first == second

        and:
        first.toString() == second.toString()
    }

    def 'test that permission of #prefix and #permission is #expected'() {
        given:
        def info = new PermissionInfo(prefix, permission, Permission.Grant.ALL)

        expect:
        info.permission == expected

        where:
        prefix       | permission           || expected
        null         | 'command.permission' || 'command.permission'
        'blocksmith' | 'command.permission' || 'blocksmith.command.permission'
    }

}
