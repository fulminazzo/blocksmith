package it.fulminazzo.blocksmith.message.receiver

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.PlayerSettings
import spock.lang.Specification

class VelocityReceiverTest extends Specification {

    def 'test that #toAudience converts receiver'() {
        given:
        def receiver = Mock(CommandSource)

        when:
        def actual = new VelocityReceiver(receiver).toAudience()

        then:
        actual != null
    }

    def 'test that getLocale returns player locale'() {
        given:
        def settings = Mock(PlayerSettings)
        settings.locale >> Locale.ITALY

        and:
        def receiver = Mock(Player)
        receiver.playerSettings >> settings

        when:
        def actual = new VelocityReceiver(receiver).locale

        then:
        actual == Locale.ITALY
    }

    def 'test that getLocale returns system locale for console'() {
        given:
        def receiver = Mock(CommandSource)

        when:
        def actual = new VelocityReceiver(receiver).locale

        then:
        actual == Locale.default
    }
    
}
