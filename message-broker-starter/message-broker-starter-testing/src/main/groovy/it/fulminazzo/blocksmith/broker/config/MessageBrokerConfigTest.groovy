package it.fulminazzo.blocksmith.broker.config

import spock.lang.Specification

abstract class MessageBrokerConfigTest extends Specification {

    def 'test that factory for config type exists'() {
        given:
        def config = configType.getConstructor(new Class[0]).newInstance(new Object[0])

        when:
        def factory = MessageBrokerFactories.FACTORIES[config.class]

        then:
        factory != null
        factoryType.isInstance(factory)
    }

    protected abstract Class<? extends MessageBrokerConfig> getConfigType()

    protected abstract Class<? extends MessageBrokerFactory> getFactoryType()

}
