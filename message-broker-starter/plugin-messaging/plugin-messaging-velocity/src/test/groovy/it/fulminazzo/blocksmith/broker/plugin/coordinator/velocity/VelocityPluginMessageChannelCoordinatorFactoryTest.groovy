package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity

import com.velocitypowered.api.proxy.ProxyServer
import it.fulminazzo.blocksmith.broker.plugin.coordinator.PluginMessageChannelCoordinatorFactory
import spock.lang.Specification

class VelocityPluginMessageChannelCoordinatorFactoryTest extends Specification {
    private final PluginMessageChannelCoordinatorFactory factory = new VelocityPluginMessageChannelCoordinatorFactory()

    def 'test that create returns VelocityPluginMessageChannelCoordinator'() {
        when:
        def coordinator = factory.create(new MockPlugin(Mock(ProxyServer)))

        then:
        VelocityPluginMessageChannelCoordinator.isInstance(coordinator)
    }

    def 'test that factory supports everything'() {
        expect:
        factory.supportsOwner(Object)
    }

    private static final class MockPlugin {
        private final ProxyServer server

        MockPlugin(final ProxyServer server) {
            this.server = server
        }

    }

}
