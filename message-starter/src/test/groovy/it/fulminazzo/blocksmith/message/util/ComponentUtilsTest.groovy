package it.fulminazzo.blocksmith.message.util

import spock.lang.Specification

class ComponentUtilsTest extends Specification {

    def 'test that subcomponent of #rawComponent from #from to #to returns #expected'() {
        given:
        def component = ComponentUtils.toComponent(rawComponent)

        when:
        def subComponent = ComponentUtils.subcomponent(component, from, to)

        then:
        ComponentUtils.toString(subComponent) == expected

        where:
        rawComponent                                                                                                  | from | to | expected
        ''                                                                                                            | 0    | 0  | ''
        'Hello, world!'                                                                                               | 0    | 5  | 'Hello'
        'Hello, world!'                                                                                               | 7    | 12 | 'world'
        'Hello, world!'                                                                                               | 0    | 13 | 'Hello, world!'
        'Hello, world!'                                                                                               | 13   | 13 | ''
        '<red>Hello, world!'                                                                                          | 0    | 13 | '<red>Hello, world!'
        '<red>Hello, world!'                                                                                          | 0    | 5  | '<red>Hello'
        '<red>Hello, world!'                                                                                          | 7    | 12 | '<red>world'
        '<red>Hello, world!'                                                                                          | 0    | 13 | '<red>Hello, world!'
        '<red>Hello, world!'                                                                                          | 13   | 13 | '<red>'
        '<red>Hello</red>, <blue>world!'                                                                              | 0    | 5  | '<red>Hello'
        '<red>Hello</red>, <blue>world!'                                                                              | 7    | 12 | '<blue>world'
        '<red>Hello</red>, <blue>world!'                                                                              | 0    | 13 | '<red>Hello</red>, <blue>world!'
        '<red>Hello</red>, <blue>world!'                                                                              | 13   | 13 | ''
        '<red>Hello</red>, <click:open_url:\'www.google.com\'>world</click>'                                          | 0    | 5  | '<red>Hello'
        '<red>Hello</red>, <click:open_url:\'www.google.com\'>world</click>'                                          | 7    | 12 | '<click:open_url:\'www.google.com\'>world'
        'Hello, <click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world</click>!' | 0    | 5  | 'Hello'
        'Hello, <click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world</click>!' | 7    | 12 |
                '<click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world'
        'Hello, <click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world</click>!' | 7    | 13 |
                '<click:copy_to_clipboard:\'<iper<super<nested<tag> even <though>> really> unlikely>\'>world</click>!'
    }

    def 'test that subcomponent of #from and #to throws'() {
        given:
        def component = ComponentUtils.toComponent('Hello, world!')

        when:
        ComponentUtils.subcomponent(component, from, to)

        then:
        thrown(IndexOutOfBoundsException)

        where:
        from | to
        -1   | 13
        14   | 13
        0    | 14
        0    | -1
        12   | 7
    }

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