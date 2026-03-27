package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import spock.lang.Specification

class BungeeCommandSenderWrapperTest extends Specification {

    def 'test that hasPermission returns #expected for #permission and #op'() {
        given:
        def sender = Mock(CommandSender)
        sender.hasPermission(_) >> { a ->
            return a[0] == 'permission'
        }

        and:
        def wrapper = new BungeeCommandSenderWrapper(Mock(ApplicationHandle), sender)

        when:
        def actual = wrapper.hasPermission(permission)

        then:
        actual == expected

        where:
        permission                                                    | op    || expected
        new PermissionInfo(null, 'permission', Permission.Grant.NONE) | true  || true
        new PermissionInfo(null, 'permission', Permission.Grant.NONE) | false || true
        new PermissionInfo(null, 'invalid', Permission.Grant.NONE)    | true  || false
        new PermissionInfo(null, 'invalid', Permission.Grant.NONE)    | false || false
        new PermissionInfo(null, 'permission', Permission.Grant.OP)   | true  || true
        new PermissionInfo(null, 'permission', Permission.Grant.OP)   | false || true
        new PermissionInfo(null, 'invalid', Permission.Grant.OP)      | true  || false
        new PermissionInfo(null, 'invalid', Permission.Grant.OP)      | false || false
        new PermissionInfo(null, 'permission', Permission.Grant.ALL)  | true  || true
        new PermissionInfo(null, 'permission', Permission.Grant.ALL)  | false || true
        new PermissionInfo(null, 'invalid', Permission.Grant.ALL)     | true  || true
        new PermissionInfo(null, 'invalid', Permission.Grant.ALL)     | false || true
    }

    def 'test that #method returns #expected for player'() {
        given:
        def player = Mock(ProxiedPlayer)
        player.name >> 'Alex'
        player.uniqueId >> UUID.nameUUIDFromBytes(player.name.bytes)

        and:
        def wrapper = new BungeeCommandSenderWrapper(Mock(ApplicationHandle), player)

        when:
        def actual = wrapper."$method"()

        then:
        actual == expected

        where:
        method     || expected
        'getName'  || 'Alex'
        'getId'    || UUID.nameUUIDFromBytes('Alex'.bytes)
        'isPlayer' || true
    }

    def 'test that #method returns #expected for console'() {
        given:
        def sender = Mock(CommandSender)
        sender.name >> '(server)'

        and:
        def wrapper = new BungeeCommandSenderWrapper(Mock(ApplicationHandle), sender)

        when:
        def actual = wrapper."$method"()

        then:
        actual == expected

        where:
        method     || expected
        'getName'  || '(server)'
        'getId'    || '(server)'
        'isPlayer' || false
    }

}
