package it.fulminazzo.blocksmith.command.node

import it.fulminazzo.blocksmith.command.annotation.Help
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import spock.lang.Specification

class HelpNodeTest extends Specification {

    def 'test merge with #baseHelp and #incomingHelp keeps expected help shape'() {
        given:
        def baseParent = parentNode('root')
        def incomingParent = parentNode('root')

        and:
        def baseNode = new HelpNode(baseHelp as Help, baseParent)
        def incomingNode = new HelpNode(incomingHelp as Help, incomingParent)

        when:
        def merged = baseNode.merge(incomingNode)

        then:
        merged.is(baseNode)
        merged.name == expectedName
        merged.aliases == expectedAliases.toSet()
        merged.commandInfo.description == expectedDescription
        merged.commandInfo.permission.permission == expectedPermission
        merged.commandInfo.permission.grant == expectedGrant

        where:
        baseHelp                                                      | incomingHelp || expectedName | expectedAliases         | expectedDescription             | expectedPermission       | expectedGrant
        help(
                [Help.DEFAULT_NAME],
                '',
                permission('', '', Permission.Grant.OP)
        )                                                             |
                help(
                        ['assist'],
                        'custom.help.description',
                        permission('custom.help', 'custom', Permission.Grant.NONE)
                )                                                                    || 'assist'     | ['assist']              | 'custom.help.description'       | 'custom.custom.help'     | Permission.Grant.NONE
        help(
                ['base-help'],
                'base.help.description',
                permission('base.help', 'base', Permission.Grant.ALL)
        )                                                             |
                help(
                        ['assist'],
                        'custom.help.description',
                        permission('custom.help', 'custom', Permission.Grant.NONE)
                )                                                                    || 'base-help'  | ['assist', 'base-help'] | 'base.help.description'         | 'base.base.help'         | Permission.Grant.ALL
        help(
                [Help.DEFAULT_NAME],
                '',
                permission('', '', Permission.Grant.OP)
        )                                                             |
                help(
                        [Help.DEFAULT_NAME],
                        '',
                        permission('', '', Permission.Grant.OP)
                )                                                                    || 'help'       | ['help']                | 'command.root.help.description' | 'permission.root.help'   | Permission.Grant.ALL
        help(
                [Help.DEFAULT_NAME],
                '',
                permission('', '', Permission.Grant.OP)
        )                                                             |
                help(
                        [Help.DEFAULT_NAME],
                        'incoming.help.description',
                        permission('', '', Permission.Grant.OP)
                )                                                                    || 'help'       | ['help']                | 'incoming.help.description'     | 'permission.root.help'   | Permission.Grant.ALL
        help(
                [Help.DEFAULT_NAME],
                '',
                permission('', '', Permission.Grant.OP)
        )                                                             |
                help(
                        [Help.DEFAULT_NAME],
                        '',
                        permission('incoming.help', 'incoming', Permission.Grant.OP)
                )                                                                    || 'help'       | ['help']                | 'command.root.help.description' | 'incoming.incoming.help' | Permission.Grant.ALL
    }

    def 'test isDefaultHelpAnnotation with #aliases #description and #permission returns #expected'() {
        given:
        def method = HelpNode.getDeclaredMethod('isDefaultHelpAnnotation', Help)
        method.accessible = true

        and:
        def annotation = help(aliases, description, permission) as Help

        expect:
        method.invoke(null, annotation) == expected

        where:
        aliases             | description               | permission                                         || expected
        [Help.DEFAULT_NAME] | ''                        | permission('', '', Permission.Grant.OP)            || true
        ['assist']          | ''                        | permission('', '', Permission.Grant.OP)            || false
        [Help.DEFAULT_NAME] | 'custom.help.description' | permission('', '', Permission.Grant.OP)            || false
        [Help.DEFAULT_NAME] | ''                        | permission('custom.help', '', Permission.Grant.OP) || false
        [Help.DEFAULT_NAME] | ''                        | permission('', 'custom', Permission.Grant.OP)      || false
        [Help.DEFAULT_NAME] | ''                        | permission('', '', Permission.Grant.NONE)          || false
    }

    private static LiteralNode parentNode(final String alias) {
        def node = new LiteralNode(alias)
        node.commandInfo = new CommandInfo(
                "command.${alias}.description",
                new PermissionInfo('permission', alias, Permission.Grant.ALL)
        )
        node
    }

    private static Help help(final List<String> aliases,
                             final String description,
                             final Permission permission) {
        return new Help() {
            @Override
            String[] aliases() {
                return aliases.toArray(new String[0])
            }

            @Override
            String description() {
                return description
            }

            @Override
            Permission permission() {
                return permission
            }

            @Override
            Class<Help> annotationType() {
                return Help
            }
        }
    }

    private static Permission permission(final String value,
                                         final String group,
                                         final Permission.Grant grant) {
        return new Permission() {
            @Override
            String value() {
                return value
            }

            @Override
            String group() {
                return group
            }

            @Override
            Permission.Grant grant() {
                return grant
            }

            @Override
            Class<Permission> annotationType() {
                return Permission
            }
        }
    }
}
