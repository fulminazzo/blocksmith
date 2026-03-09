package it.fulminazzo.blocksmith.message.provider

import net.kyori.adventure.text.minimessage.MiniMessage
import spock.lang.Specification

class SimpleMessageProviderTest extends Specification {

    def 'test getMessage correctly formats all color formats'() {
        given:
        def message = '§cHello&6, &aworld&d! <aqua>How &#ff00aaare §x§1§2§3§4§5§6you <#abcdef>?'

        and:
        def expected = MiniMessage.miniMessage().deserialize(
                '<red>Hello<gold>, <green>world<light_purple>! <aqua>How <#ff00aa>are <#123456>you<#abcdef>?'
        )

        and:
        def provider = new SimpleMessageProvider([
                'message': message
        ])

        when:
        def actual = provider.getMessage('message', Locale.ITALIAN)

        then:
        actual == expected
    }

}
