package it.fulminazzo.blocksmith.message.receiver

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Player
import spock.lang.Specification

class BukkitReceiverFactoryTest extends Specification {

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType)

        then:
        (factory instanceof BukkitReceiverFactory)

        where:
        receiverType << [
                CommandSender,
                ConsoleCommandSender,
                RemoteConsoleCommandSender,
                Player
        ]
    }

}
