package it.fulminazzo.blocksmith.data.config

import spock.lang.Specification

abstract class DataSourceConfigTest extends Specification {

    def 'test that factory for config type exists'() {
        given:
        def config = configType.getConstructor(new Class[0]).newInstance(new Object[0])

        when:
        def factory = DataSourceFactories.FACTORIES[config.class]

        then:
        factory != null
        factoryType.isInstance(factory)
    }

    protected abstract Class<? extends DataSourceConfig> getConfigType()

    protected abstract Class<? extends DataSourceFactory> getFactoryType()

}
