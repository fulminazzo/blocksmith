package it.fulminazzo.blocksmith.broker.plugin

import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

class PluginMessageBrokerTest extends Specification {

    def 'test broker life cycle'() {
        given:
        def registrar = Mock(PluginMessageRegistrar)
        registrar.name >> 'blocksmith'

        and:
        def messageBroker = PluginMessageBroker.create(registrar, MapperFormat.JSON.newMapper())

        when:
        def directChannel = messageBroker.newChannel(
                new PluginMessageChannelSettings()
                        .withChannelName('main')
                        .direct('sub')
        )

        then:
        directChannel != null

        when:
        def broadcastChannel = messageBroker.newChannel(
                new PluginMessageChannelSettings()
                        .withChannelName('main')
                        .broadcast()
        )

        then:
        broadcastChannel != null

        when:
        messageBroker.close()

        then:
        noExceptionThrown()
    }
    
}
