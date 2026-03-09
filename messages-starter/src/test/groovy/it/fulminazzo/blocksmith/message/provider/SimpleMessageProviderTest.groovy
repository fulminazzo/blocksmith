package it.fulminazzo.blocksmith.message.provider

import net.kyori.adventure.text.minimessage.MiniMessage
import spock.lang.Specification

class SimpleMessageProviderTest extends Specification {

    def 'test getMessage correctly formats all color formats'() {
        given:
        def message = '§cHello, &aworld! <aqua>How are you?'

        and:
        def expected = MiniMessage.miniMessage().deserialize(
                '<red>Hello, <green>world! <aqua>How are you?'
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
