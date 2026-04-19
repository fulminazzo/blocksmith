package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.ServerApplication
import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.CommandSenderWrapper
import it.fulminazzo.blocksmith.command.MockCommandSenderWrapper
import spock.lang.Specification

class CommandSenderWrapperReceiverFactoryTest extends Specification {
    private ServerApplication application

    void setup() {
        application = Mock(ServerApplication)
    }

    def 'test that getAllReceivers returns all the receivers'() {
        given:
        def factory = new CommandSenderWrapperReceiverFactory().setup(application)

        when:
        def receivers = factory.allReceivers

        then:
        receivers != null

        when:
        def internalReceivers = receivers.collect { it.internal }

        then:
        internalReceivers.sort() == []
    }

    def 'test that create does not throw'() {
        given:
        def sender = new CommandSender()

        and:
        def factory = new CommandSenderWrapperReceiverFactory().setup(application)

        when:
        def receiver = factory.create(new MockCommandSenderWrapper(sender))

        then:
        receiver != null

        and:
        receiver.internal() == sender
    }

    def 'test that ReceiverFactories returns correct factory for #receiverType'() {
        when:
        def factory = ReceiverFactories.get(receiverType, application)

        then:
        (factory instanceof CommandSenderWrapperReceiverFactory)

        where:
        receiverType << [
                CommandSenderWrapper,
                MockCommandSenderWrapper
        ]
    }

}
