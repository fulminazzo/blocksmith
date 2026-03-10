package it.fulminazzo.blocksmith.message.receiver

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ConnectedPlayer
import net.md_5.bungee.api.connection.ProxiedPlayer
import spock.lang.Specification

class BungeeReceiverFactoryTest extends Specification {

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType)

        then:
        (factory instanceof BungeeReceiverFactory)

        where:
        receiverType << [
                CommandSender,
                ConnectedPlayer,
                ProxiedPlayer
        ]
    }

}
