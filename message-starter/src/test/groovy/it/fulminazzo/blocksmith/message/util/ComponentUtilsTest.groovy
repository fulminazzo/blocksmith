package it.fulminazzo.blocksmith.message.util

import spock.lang.Specification

class ComponentUtilsTest extends Specification {

    def 'test that actualLength of #rawComponent returns #expected'() {
        given:
        def component = ComponentUtils.toComponent(rawComponent)

        when:
        def length = ComponentUtils.actualLength(component)

        then:
        length == expected

        where:
        rawComponent                                                                                                  || expected
        ''                                                                                                            || 0
        '<red>'                                                                                                       || 0
        '<red></red>'                                                                                                 || 0
        '<red><green></green>'                                                                                        || 0
        '<bold><italic></italic></bold>'                                                                              || 0
        'x'                                                                                                           || 1
        '   '                                                                                                         || 3
        '12345'                                                                                                       || 5
        'Hello, world!'                                                                                               || 13
        '>'                                                                                                           || 1
        'Hello > world'                                                                                               || 13
        '<red>Hello, world!'                                                                                          || 13
        '<red>Hello</red>, world!'                                                                                    || 13
        'Hello, world<red>!'                                                                                          || 13
        'Hello, world<red>'                                                                                           || 12
        '<red>Hello, world!\t'                                                                                        || 14
        '<red>Hello</red> <blue>world</blue>'                                                                         || 11
        '<red><bold>Hello, world!</bold></red>'                                                                       || 13
        '<bold><italic>Hello</italic></bold>'                                                                         || 5
        '<red>Hello, <world>!'                                                                                        || 16
        '<hover:show_text:"<Hello>, world!">Hello, world!'                                                            || 13
        '<hover:show_text:"<Hello\\>, world!">Hello, world!'                                                          || 13
        'Hello, <click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world</click>!' || 13
        '<gradient:red:blue>Rainbow</gradient>'                                                                       || 7
        '<click:run_command:\'/say hello\'>Click me</click>'                                                          || 8
        'Hello <rainbow>world</rainbow>, isn\'t ' +
                '<blue><u><click:open_url:\'https://docs.papermc.io/adventure/minimessage/\'>' +
                'MiniMessage' +
                '</click></u></blue> fun?'                                                                            || 35
    }

}