package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class InjectedNodeTest extends Specification {

    def 'test that initialize with #description and #permission sets #expectedDescription and #expectedPermission'() {
        given:
        def parent = new LiteralNode('root')
        parent.commandInfo = new CommandInfo(
                'command.root.description',
                new PermissionInfo('permission', 'root', Permission.Grant.ALL)
        )

        when:
        def node = new MockInjectedNode(new String[]{'confirmation'}, description, permission, parent)

        then:
        def info = node.commandInfo
        info.description == expectedDescription
        info.permission.permission == expectedPermission
        info.permission.grant == parent.commandInfo.permission.grant

        where:
        description                | permission                || expectedDescription                     | expectedPermission
        ''                         | ''                        || 'command.root.confirmation.description' | 'permission.root.confirmation'
        'confirmation.description' | ''                        || 'confirmation.description'              | 'permission.root.confirmation'
        ''                         | 'confirmation.permission' || 'command.root.confirmation.description' | 'permission.confirmation.permission'
        'confirmation.description' | 'confirmation.permission' || 'confirmation.description'              | 'permission.confirmation.permission'
    }

    private static final class MockInjectedNode extends InjectedNode {

        /**
         * Instantiates a new Mock injected node.
         *
         * @param aliases the aliases
         * @param description the description
         * @param permission the permission
         * @param parent the parent node
         */
        MockInjectedNode(final @NotNull String[] aliases,
                         final @NotNull String description,
                         final @NotNull String permission,
                         final @NotNull LiteralNode parent) {
            super(aliases, description, permission, parent)
        }

    }

}
