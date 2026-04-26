package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class InjectedNodeTest extends Specification {

    def 'test that initialize with #description, #permission and #grant sets #expectedDescription, #expectedPermission and #expectedGrant'() {
        given:
        def parent = new LiteralNode('root')
        parent.commandInfo = new CommandInfo(
                'command.root.description',
                new PermissionInfo('permission', 'root', Permission.Grant.ALL)
        )

        and:
        def permissionAnnotation = Mock(Permission)
        permissionAnnotation.group() >> 'group'
        permissionAnnotation.value() >> permission
        permissionAnnotation.grant() >> grant

        when:
        def node = new MockInjectedNode(new String[]{'confirmation'}, description, permissionAnnotation, parent)

        then:
        def info = node.commandInfo
        info.description == expectedDescription
        info.permission.permission == expectedPermission
        info.permission.grant == expectedGrant

        where:
        description                | permission                | grant                 || expectedDescription                     | expectedPermission              | expectedGrant
        ''                         | ''                        | Permission.Grant.OP   || 'command.root.confirmation.description' | 'permission.root.confirmation'  | Permission.Grant.ALL
        'confirmation.description' | ''                        | Permission.Grant.OP   || 'confirmation.description'              | 'permission.root.confirmation'  | Permission.Grant.ALL
        ''                         | 'confirmation.permission' | Permission.Grant.OP   || 'command.root.confirmation.description' | 'group.confirmation.permission' | Permission.Grant.ALL
        'confirmation.description' | 'confirmation.permission' | Permission.Grant.OP   || 'confirmation.description'              | 'group.confirmation.permission' | Permission.Grant.ALL
        ''                         | ''                        | Permission.Grant.NONE || 'command.root.confirmation.description' | 'permission.root.confirmation'  | Permission.Grant.NONE
        'confirmation.description' | ''                        | Permission.Grant.NONE || 'confirmation.description'              | 'permission.root.confirmation'  | Permission.Grant.NONE
        ''                         | 'confirmation.permission' | Permission.Grant.NONE || 'command.root.confirmation.description' | 'group.confirmation.permission' | Permission.Grant.NONE
        'confirmation.description' | 'confirmation.permission' | Permission.Grant.NONE || 'confirmation.description'              | 'group.confirmation.permission' | Permission.Grant.NONE
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
                         final @NotNull Permission permission,
                         final @NotNull LiteralNode parent) {
            super(aliases, description, permission, parent)
        }

    }

}
