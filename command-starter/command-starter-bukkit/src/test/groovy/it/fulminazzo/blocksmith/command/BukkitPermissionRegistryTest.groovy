package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandInfo
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.permissions.PermissionDefault
import spock.lang.Specification

@Slf4j
class BukkitPermissionRegistryTest extends Specification {

    private ApplicationHandle application

    private BukkitPermissionRegistry registry

    void setup() {
        MockBukkit.mock()

        application = Mock(ApplicationHandle)
        application.server >> Bukkit.server
        application.log >> log
        application.messenger >> new Messenger(log)
        application.name >> 'blocksmith'

        registry = new BukkitPermissionRegistry(application)
    }

    void cleanup() {
        MockBukkit.unmock()
    }

    def 'test that onRegister correctly registers new command permission'() {
        given:
        def pluginManager = Bukkit.server.pluginManager
        def previousPermission = new org.bukkit.permissions.Permission(
                'help',
                PermissionDefault.TRUE
        )
        pluginManager.addPermission(previousPermission)

        and:
        def node = new LiteralNode('help', '?')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo('help', Permission.Grant.ALL)
        )
        def plugin = new LiteralNode('plugin')
        plugin.commandInfo = new CommandInfo(
                'command.description.help.plugin',
                new PermissionInfo('help.plugin', Permission.Grant.OP)
        )
        node.addChild(plugin)
        def name = new ArgumentNode('name', String, false)
        plugin.addChild(name)

        when:
        registry.registerPermission(node)

        then:
        noExceptionThrown()

        and:
        def helpPermission = pluginManager.getPermission('help')
        helpPermission != previousPermission
        (helpPermission instanceof BukkitPermissionRegistry.BukkitPermission)
        helpPermission.name == 'help'
        helpPermission.default == PermissionDefault.TRUE
        helpPermission.children.keySet().toList() == ['help.plugin']

        and:
        def helpPluginPermission = pluginManager.getPermission('help.plugin')
        (helpPluginPermission instanceof BukkitPermissionRegistry.BukkitPermission)
        helpPluginPermission.name == 'help.plugin'
        helpPluginPermission.default == PermissionDefault.OP
        helpPluginPermission.children.keySet().toList() == []

        and:
        def previousPermissions = registry.previousPermissions
        previousPermissions.size() == 1

        and:
        previousPermissions['help'] == previousPermission
    }

    def 'test that onUnregister removes command permissions and restores previous permissions'() {
        given:
        def previousPermission = new org.bukkit.permissions.Permission(
                'help',
                PermissionDefault.TRUE
        )
        registry.previousPermissions['help'] = previousPermission

        and:
        def pluginManager = Bukkit.server.pluginManager
        pluginManager.addPermission(new BukkitPermissionRegistry.BukkitPermission(
                new PermissionInfo('help', Permission.Grant.ALL),
                ['help.plugin']
        ))
        def otherPermission = new org.bukkit.permissions.Permission('help.plugin')
        pluginManager.addPermission(otherPermission)

        when:
        registry.unregisterPermission('help')

        then:
        noExceptionThrown()

        and:
        def helpPermission = pluginManager.getPermission('help')
        helpPermission == previousPermission

        and:
        def helpPluginPermission = pluginManager.getPermission('help.plugin')
        helpPluginPermission == otherPermission

        and:
        def previousPermissions = registry.previousPermissions
        previousPermissions.isEmpty()
    }

}
