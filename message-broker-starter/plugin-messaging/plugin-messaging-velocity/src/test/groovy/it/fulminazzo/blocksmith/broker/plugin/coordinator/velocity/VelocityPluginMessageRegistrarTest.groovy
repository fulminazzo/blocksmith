package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity

import com.velocitypowered.api.proxy.ProxyServer
import it.fulminazzo.blocksmith.reflect.ReflectException
import spock.lang.Specification

class VelocityPluginMessageRegistrarTest extends Specification {

    def 'test that registrar returns correct data'() {
        given:
        def server = Mock(ProxyServer)
        def plugin = new MockPlugin(server)

        and:
        def registrar = new VelocityPluginMessageRegistrar(plugin)

        expect:
        registrar.plugin() == plugin
        registrar.server() == server
    }

    def 'test that server method throws if field not found'() {
        given:
        def registrar = new VelocityPluginMessageRegistrar(this)

        when:
        registrar.server()

        then:
        thrown(ReflectException)
    }

    private static final class MockPlugin {
        private final ProxyServer server

        MockPlugin(final ProxyServer server) {
            this.server = server
        }

    }

}
