//TODO: update
//package it.fulminazzo.blocksmith.command
//
//import com.velocitypowered.api.command.CommandSource
//import com.velocitypowered.api.proxy.ConsoleCommandSource
//import com.velocitypowered.api.proxy.Player
//import it.fulminazzo.blocksmith.ApplicationHandle
//import it.fulminazzo.blocksmith.command.annotation.Permission
//import it.fulminazzo.blocksmith.command.node.PermissionInfo
//import spock.lang.Specification
//
//class VelocityCommandSenderWrapperTest extends Specification {
//
//    def 'test that hasPermission returns #expected for #permission and #op'() {
//        given:
//        def sender = Mock(CommandSource)
//        sender.hasPermission(_) >> { a ->
//            return a[0] == 'permission'
//        }
//
//        and:
//        def wrapper = new VelocityCommandSenderWrapper(Mock(ApplicationHandle), sender)
//
//        when:
//        def actual = wrapper.hasPermission(permission)
//
//        then:
//        actual == expected
//
//        where:
//        permission                                              | op    || expected
//        new PermissionInfo(null, 'permission', Permission.Grant.NONE) | true  || true
//        new PermissionInfo(null, 'permission', Permission.Grant.NONE) | false || true
//        new PermissionInfo(null, 'invalid', Permission.Grant.NONE)    | true  || false
//        new PermissionInfo(null, 'invalid', Permission.Grant.NONE)    | false || false
//        new PermissionInfo(null, 'permission', Permission.Grant.OP)   | true  || true
//        new PermissionInfo(null, 'permission', Permission.Grant.OP)   | false || true
//        new PermissionInfo(null, 'invalid', Permission.Grant.OP)      | true  || false
//        new PermissionInfo(null, 'invalid', Permission.Grant.OP)      | false || false
//        new PermissionInfo(null, 'permission', Permission.Grant.ALL)  | true  || true
//        new PermissionInfo(null, 'permission', Permission.Grant.ALL)  | false || true
//        new PermissionInfo(null, 'invalid', Permission.Grant.ALL)     | true  || true
//        new PermissionInfo(null, 'invalid', Permission.Grant.ALL)     | false || true
//    }
//
//    def 'test that #method returns #expected for player'() {
//        given:
//        def player = Mock(Player)
//        player.username >> 'Alex'
//        player.uniqueId >> UUID.nameUUIDFromBytes(player.username.bytes)
//
//        and:
//        def wrapper = new VelocityCommandSenderWrapper(Mock(ApplicationHandle), player)
//
//        when:
//        def actual = wrapper."$method"()
//
//        then:
//        actual == expected
//
//        where:
//        method     || expected
//        'getName'  || 'Alex'
//        'getId'    || UUID.nameUUIDFromBytes('Alex'.bytes)
//        'isPlayer' || true
//    }
//
//    def 'test that #method returns #expected for console'() {
//        given:
//        def sender = Mock(ConsoleCommandSource)
//
//        and:
//        def wrapper = new VelocityCommandSenderWrapper(Mock(ApplicationHandle), sender)
//
//        when:
//        def actual = wrapper."$method"()
//
//        then:
//        actual == expected
//
//        where:
//        method     || expected
//        'getName'  || 'console'
//        'getId'    || 'console'
//        'isPlayer' || false
//    }
//
//}
