package it.fulminazzo.blocksmith.message.receiver

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import spock.lang.Specification

class VelocityReceiverFactoryTest extends Specification {

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType)

        then:
        (factory instanceof VelocityReceiverFactory)

        where:
        receiverType << [
                CommandSource,
                ConsoleCommandSource,
                Player
        ]
    }

}
