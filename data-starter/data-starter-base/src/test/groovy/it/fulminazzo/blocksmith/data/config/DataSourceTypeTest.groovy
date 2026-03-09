package it.fulminazzo.blocksmith.data.config

import spock.lang.Specification

class DataSourceTypeTest extends Specification {

    def 'test that getConfigClass throws for type #dataSourceType'() {
        given:
        def lowercaseType = dataSourceType.name().toLowerCase()
        if (dataSourceType == DataSourceType.CACHED) lowercaseType = 'cache'

        when:
        dataSourceType.getConfigClass()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${DataSourceConfig.simpleName} for ${dataSourceType.name().toLowerCase().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:data-starter-${lowercaseType} " +
                "is correctly installed."

        where:
        dataSourceType << DataSourceType.values()
    }

}
