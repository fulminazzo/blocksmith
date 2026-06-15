package it.fulminazzo.blocksmith.broker.plugin.coordinator

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class ProxyPluginMessageChannelCoordinatorTest extends Specification {
    private final ProxyPluginMessageChannelCoordinator<String> coordinator = Spy(
            ProxyPluginMessageChannelCoordinator,
            constructorArgs : [Mock(PluginMessageRegistrar)]
    )

    def 'test that getNodePublisher creates a new node if missing'() {
        given:
        def nodes = Reflect.on(coordinator).get('nodes').get() as Map
        final nodeId = 'node'

        and:
        coordinator.getNodePublisher(_) >> { callRealMethod() }
        coordinator.newNodePublisher(_) >> { Mock(PluginMessagePublisher) }

        when:
        def first = coordinator.getNodePublisher(nodeId)

        then:
        first != null

        and:
        nodes[nodeId] == first

        when:
        def second = coordinator.getNodePublisher(nodeId)

        then:
        second != null

        and:
        first == second
    }

    def 'test that refreshNodes removes disconnected nodes and updates with new ones'() {
        given:
        def nodes = Reflect.on(coordinator).get('nodes').get() as Map
        for (def i in 1..5)
            nodes.put("$i", Mock(PluginMessagePublisher))

        and:
        final newNodes = (1..3).collect { "${it * 2}" }

        and:
        coordinator.allNodes >> newNodes
        coordinator.newNodePublisher(_) >> Mock(PluginMessagePublisher)

        when:
        Reflect.on(coordinator).invoke('refreshNodes')

        then:
        nodes.size() == 3
        nodes.keySet() == newNodes.toSet()
    }

    def 'test that #method delegates to nodes'() {
        given:
        def nodes = (1..5).collect { "$it".toString() }
        def publishers = nodes.collectEntries { [it, Mock(PluginMessagePublisher)] }
        coordinator.allNodes >> nodes
        coordinator.newNodePublisher(_) >> { a -> publishers[a[0]] }

        when:
        coordinator."$method"(*arguments)

        then:
        nodes.forEach {
            1 * publishers[it]."$method"(*_)
        }

        where:
        method                    | arguments
        'publish'                 | ['channel', 'message'.bytes]
        'republishFailedMessages' | []
    }

}
