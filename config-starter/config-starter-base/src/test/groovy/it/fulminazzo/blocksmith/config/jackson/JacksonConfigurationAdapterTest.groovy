package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import spock.lang.Specification

class JacksonConfigurationAdapterTest extends Specification {

    def 'test that applyNamingStrategy works'() {
        given:
        def data = ['secretGreeting' : 'Hello, world']

        when:
        JacksonConfigurationAdapter.applyNamingStrategy(data, strategy)

        then:
        data == expected

        where:
        strategy                                || expected
        null                                    || ['secretGreeting' : 'Hello, world']
        PropertyNamingStrategies.LOWER_DOT_CASE || ['secret.greeting' : 'Hello, world']
        PropertyNamingStrategies.SNAKE_CASE     || ['secret_greeting' : 'Hello, world']
        PropertyNamingStrategies.KEBAB_CASE     || ['secret-greeting' : 'Hello, world']
    }

    def 'test that unapplyNamingStrategy works'() {
        when:
        JacksonConfigurationAdapter.unapplyNamingStrategy(data, strategy)

        then:
        data == ['secretGreeting' : 'Hello, world']

        where:
        data                                 | strategy
        ['secretGreeting' : 'Hello, world']  | null
        ['secretGreeting' : 'Hello, world']  | PropertyNamingStrategies.LOWER_DOT_CASE
        ['secret_greeting' : 'Hello, world'] | PropertyNamingStrategies.SNAKE_CASE
        ['secret-greeting' : 'Hello, world'] | PropertyNamingStrategies.KEBAB_CASE
    }

}
