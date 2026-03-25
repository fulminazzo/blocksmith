package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import spock.lang.Specification

class BukkitCommandSenderWrapperTest extends Specification {

    def 'test that hasPermission returns #expected for #permission and #op'() {
        given:
        def sender = Mock(CommandSender)
        sender.hasPermission(_) >> { a ->
            return a[0] == 'permission'
        }
        sender.op >> op

        and:
        def wrapper = new BukkitCommandSenderWrapper(sender)

        when:
        def actual = wrapper.hasPermission(permission)

        then:
        actual == expected

        where:
        permission                                              | op    || expected
        new PermissionInfo('permission', Permission.Grant.NONE) | true  || true
        new PermissionInfo('permission', Permission.Grant.NONE) | false || true
        new PermissionInfo('invalid', Permission.Grant.NONE)    | true  || false
        new PermissionInfo('invalid', Permission.Grant.NONE)    | false || false
        new PermissionInfo('permission', Permission.Grant.OP)   | true  || true
        new PermissionInfo('permission', Permission.Grant.OP)   | false || true
        new PermissionInfo('invalid', Permission.Grant.OP)      | true  || true
        new PermissionInfo('invalid', Permission.Grant.OP)      | false || false
        new PermissionInfo('permission', Permission.Grant.ALL)  | true  || true
        new PermissionInfo('permission', Permission.Grant.ALL)  | false || true
        new PermissionInfo('invalid', Permission.Grant.ALL)     | true  || true
        new PermissionInfo('invalid', Permission.Grant.ALL)     | false || true
    }

    def 'test that #method returns #expected for player'() {
        given:
        def player = Mock(Player)
        player.name >> 'Alex'
        player.uniqueId >> UUID.nameUUIDFromBytes(player.name.bytes)

        and:
        def wrapper = new BukkitCommandSenderWrapper(player)

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
        def sender = Mock(ConsoleCommandSender)
        sender.name >> 'CONSOLE'

        and:
        def wrapper = new BukkitCommandSenderWrapper(sender)

        when:
        def actual = wrapper."$method"()

        then:
        actual == expected

        where:
        method     || expected
        'getName'  || 'CONSOLE'
        'getId'    || 'CONSOLE'
        'isPlayer' || false
    }

}
