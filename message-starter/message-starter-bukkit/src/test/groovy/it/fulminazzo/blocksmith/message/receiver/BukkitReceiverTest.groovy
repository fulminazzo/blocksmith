package it.fulminazzo.blocksmith.message.receiver

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import spock.lang.Specification

class BukkitReceiverTest extends Specification {

    private BukkitAudiences adventure

    void setup() {
        adventure = Mock(BukkitAudiences)
        adventure.sender(_) >> Mock(Audience)
    }

    def 'test that #audience converts receiver'() {
        given:
        def receiver = Mock(CommandSender)

        when:
        def actual = new BukkitReceiver(adventure, receiver).audience()

        then:
        actual != null
    }

    def 'test that #audience returns same audience receiver'() {
        given:
        def receiver = Mock(CommandSender, additionalInterfaces: [Audience])

        when:
        def actual = new BukkitReceiver(adventure, receiver).audience()

        then:
        actual == receiver
    }

    def 'test that getLocale returns player locale'() {
        given:
        def receiver = Mock(Player)
        receiver.locale >> Locale.ITALY.toString()

        when:
        def actual = new BukkitReceiver(adventure, receiver).locale

        then:
        actual == Locale.ITALY
    }

    def 'test that getLocale returns system locale for console'() {
        given:
        def receiver = Mock(ConsoleCommandSender)

        when:
        def actual = new BukkitReceiver(adventure, receiver).locale

        then:
        actual == Locale.default
    }

}
