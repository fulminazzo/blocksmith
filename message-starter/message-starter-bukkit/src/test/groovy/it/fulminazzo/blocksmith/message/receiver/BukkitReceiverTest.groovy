package it.fulminazzo.blocksmith.message.receiver

import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import spock.lang.Specification

class BukkitReceiverTest extends Specification {

    def 'test that #toAudience converts receiver'() {
        given:
        def receiver = Mock(CommandSender)

        when:
        def actual = new BukkitReceiver(receiver).toAudience()

        then:
        actual != null
    }

    def 'test that #toAudience returns same audience receiver'() {
        given:
        def receiver = Mock(CommandSender, additionalInterfaces: [Audience])

        when:
        def actual = new BukkitReceiver(receiver).toAudience()

        then:
        actual == receiver
    }

    def 'test that getLocale returns player locale'() {
        given:
        def receiver = Mock(Player)
        receiver.locale >> Locale.ITALY.toString()

        when:
        def actual = new BukkitReceiver(receiver).locale

        then:
        actual == Locale.ITALY
    }

    def 'test that getLocale returns system locale for console'() {
        given:
        def receiver = Mock(ConsoleCommandSender)

        when:
        def actual = new BukkitReceiver(receiver).locale

        then:
        actual == Locale.default
    }

}
