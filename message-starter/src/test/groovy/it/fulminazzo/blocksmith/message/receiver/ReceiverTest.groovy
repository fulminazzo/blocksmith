package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.message.Player
import it.fulminazzo.blocksmith.message.util.ComponentUtils
import it.fulminazzo.blocksmith.reflect.Reflect
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

class ReceiverTest extends Specification {
    private static final Title.Times DEFAULT_TIMES = Title.Times.times(
            Duration.of(1L, ChronoUnit.SECONDS),
            Duration.of(2L, ChronoUnit.SECONDS),
            Duration.of(1L, ChronoUnit.SECONDS)
    )

    private Player player
    private Receiver receiver

    void setup() {
        player = new Player('Luke')
        receiver = new PlayerReceiver(player)
    }

    def 'test that sendTitle of #arguments sends #expectedTitle, #expectedSubtitle and #expectedTimes'() {
        given:
        def reflect = Reflect.on(receiver)
        def method = reflect.getMethod(Receiver, 'sendTitle', *arguments.collect {
            it == null ? Component : it.class
        })

        when:
        reflect.invoke(method, *arguments.collect { it == 'null' ? null : it })

        and:
        def lastTitle = player.lastTitle

        then:
        lastTitle[TitlePart.TITLE] == expectedTitle

        and:
        lastTitle[TitlePart.SUBTITLE] == expectedSubtitle

        and:
        lastTitle[TitlePart.TIMES] == expectedTimes

        where:
        arguments                                                            || expectedTitle | expectedSubtitle           | expectedTimes
        // String
        ['null']                                                             ||
                null                                                                          | null                       | null
        ['title']                                                            ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        // Component
        [(Component) null]                                                   ||
                null                                                                          | null                       | null
        [Component.text('title')]                                            ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        // String, Times
        ['null', DEFAULT_TIMES]                                              ||
                null                                                                          | null                       | null
        ['title', DEFAULT_TIMES]                                             ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        // Component, Times
        [(Component) null, DEFAULT_TIMES]                                    ||
                null                                                                          | null                       | null
        [Component.text('title'), DEFAULT_TIMES]                             ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        // String, String
        ['null', 'null']                                                     ||
                null                                                                          | null                       | null
        ['null', 'subtitle']                                                 ||
                Component.empty()                                                             | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        ['title', 'null']                                                    ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        ['title', 'subtitle']                                                ||
                Component.text('title')                                                       | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        // String, Component
        ['null', (Component) null]                                           ||
                null                                                                          | null                       | null
        ['null', Component.text('subtitle')]                                 ||
                Component.empty()                                                             | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        ['title', (Component) null]                                          ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        ['title', Component.text('subtitle')]                                ||
                Component.text('title')                                                       | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        // Component, String
        [(Component) null, 'null']                                           ||
                null                                                                          | null                       | null
        [(Component) null, 'subtitle']                                       ||
                Component.empty()                                                             | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        [Component.text('title'), 'null']                                    ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        [Component.text('title'), 'subtitle']                                ||
                Component.text('title')                                                       | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        // Component, Component
        [(Component) null, (Component) null]                                 ||
                null                                                                          | null                       | null
        [(Component) null, Component.text('subtitle')]                       ||
                Component.empty()                                                             | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        [Component.text('title'), (Component) null]                          ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        [Component.text('title'), Component.text('subtitle')]                ||
                Component.text('title')                                                       | Component.text('subtitle') | Receiver.DEFAULT_TIMES
        // String, String, Times
        ['null', 'null', DEFAULT_TIMES]                                      ||
                null                                                                          | null                       | null
        ['null', 'subtitle', DEFAULT_TIMES]                                  ||
                Component.empty()                                                             | Component.text('subtitle') | DEFAULT_TIMES
        ['title', 'null', DEFAULT_TIMES]                                     ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        ['title', 'subtitle', DEFAULT_TIMES]                                 ||
                Component.text('title')                                                       | Component.text('subtitle') | DEFAULT_TIMES
        // String, Component, Times
        ['null', (Component) null, DEFAULT_TIMES]                            ||
                null                                                                          | null                       | null
        ['null', Component.text('subtitle'), DEFAULT_TIMES]                  ||
                Component.empty()                                                             | Component.text('subtitle') | DEFAULT_TIMES
        ['title', (Component) null, DEFAULT_TIMES]                           ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        ['title', Component.text('subtitle'), DEFAULT_TIMES]                 ||
                Component.text('title')                                                       | Component.text('subtitle') | DEFAULT_TIMES
        // Component, String, Times
        [(Component) null, 'null', DEFAULT_TIMES]                            ||
                null                                                                          | null                       | null
        [(Component) null, 'subtitle', DEFAULT_TIMES]                        ||
                Component.empty()                                                             | Component.text('subtitle') | DEFAULT_TIMES
        [Component.text('title'), 'null', DEFAULT_TIMES]                     ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        [Component.text('title'), 'subtitle', DEFAULT_TIMES]                 ||
                Component.text('title')                                                       | Component.text('subtitle') | DEFAULT_TIMES
        // Component, Component, Times
        [(Component) null, (Component) null, DEFAULT_TIMES]                  ||
                null                                                                          | null                       | null
        [(Component) null, Component.text('subtitle'), DEFAULT_TIMES]        ||
                Component.empty()                                                             | Component.text('subtitle') | DEFAULT_TIMES
        [Component.text('title'), (Component) null, DEFAULT_TIMES]           ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        [Component.text('title'), Component.text('subtitle'), DEFAULT_TIMES] ||
                Component.text('title')                                                       | Component.text('subtitle') | DEFAULT_TIMES
        // Component, Component, Times empty
        [Component.text(' '), Component.text(' '), DEFAULT_TIMES]            ||
                null                                                                          | null                       | null
        [Component.text(' '), Component.text('subtitle'), DEFAULT_TIMES]     ||
                Component.text(' ')                                                           | Component.text('subtitle') | DEFAULT_TIMES
        [Component.text('title'), Component.text(' '), DEFAULT_TIMES]        ||
                Component.text('title')                                                       | Component.text(' ')        | DEFAULT_TIMES
        [Component.text('title'), Component.text('subtitle'), DEFAULT_TIMES] ||
                Component.text('title')                                                       | Component.text('subtitle') | DEFAULT_TIMES
    }

    def 'test that sendSubtitle of #arguments sends #expectedTitle, #expectedSubtitle and #expectedTimes'() {
        given:
        def reflect = Reflect.on(receiver)
        def method = reflect.getMethod(Receiver, 'sendSubtitle', *arguments.collect {
            it == null ? Component : it.class
        })

        when:
        reflect.invoke(method, *arguments.collect { it == 'null' ? null : it })

        and:
        def lastTitle = player.lastTitle

        then:
        lastTitle[TitlePart.TITLE] == expectedTitle

        and:
        lastTitle[TitlePart.SUBTITLE] == expectedSubtitle

        and:
        lastTitle[TitlePart.TIMES] == expectedTimes

        where:
        arguments                                                            || expectedTitle | expectedSubtitle           | expectedTimes
        // String
        ['null']                                                             ||
                null                                                                          | null                       | null
        ['title']                                                            ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        // Component
        [(Component) null]                                                   ||
                null                                                                          | null                       | null
        [Component.text('title')]                                            ||
                Component.text('title')                                                       | Component.empty()          | Receiver.DEFAULT_TIMES
        // String, Times
        ['null', DEFAULT_TIMES]                                              ||
                null                                                                          | null                       | null
        ['title', DEFAULT_TIMES]                                             ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
        // Component, Times
        [(Component) null, DEFAULT_TIMES]                                    ||
                null                                                                          | null                       | null
        [Component.text('title'), DEFAULT_TIMES]                             ||
                Component.text('title')                                                       | Component.empty()          | DEFAULT_TIMES
    }

    def 'test that sendActionBar #message works'() {
        given:
        def expected = 'Hello, world!'

        when:
        receiver.sendActionBar(message)

        then:
        ComponentUtils.toString(player.lastMessage) == expected

        where:
        message << ['Hello, world!', Component.text('Hello, world!')]
    }

    def 'test that sendMessage #message works'() {
        given:
        def expected = 'Hello, world!'

        when:
        receiver.sendMessage(message)

        then:
        ComponentUtils.toString(player.lastMessage) == expected

        where:
        message << ['Hello, world!', Component.text('Hello, world!')]
    }

}
