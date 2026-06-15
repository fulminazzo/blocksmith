package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity

import com.velocitypowered.api.proxy.ProxyServer
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageRegistrar
import spock.lang.Specification

class VelocityPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageRegistrar validRegistrar = Mock(PluginMessageRegistrar) {
        it.server() >> Mock(ProxyServer)
    }

    private final PluginMessageRegistrar invalidRegistrar = Mock(PluginMessageRegistrar) {
        it.plugin() >> new Object()
        it.server() >> new Object()
    }

    private final PluginMessageChannelCoordinatorFactory factory = new VelocityPluginMessageChannelCoordinatorFactory()

    def 'test that create returns VelocityPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(validRegistrar)

        then:
        VelocityPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports Plugin'() {
        expect:
        factory.supportsRegistrar(validRegistrar)

        and:
        !factory.supportsRegistrar(invalidRegistrar)
    }
    
}
