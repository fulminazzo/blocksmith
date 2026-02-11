package it.fulminazzo.blocksmith.data.redis

import spock.lang.Specification

class RedisDataSourceBuilderTest extends Specification {

    def 'test that getRedisUrl with #encrypted, #username and #password returns #expected'() {
        given:
        def builder = new RedisDataSourceBuilder()
                .host('127.0.0.1')
                .port(1337)
                .database(1)
                .encrypted(encrypted)
                .username(username)
                .password(password)

        when:
        def url = builder.redisUrl

        then:
        url == expected

        where:
        encrypted | username     | password              || expected
        false     | null         | null                  || 'redis://127.0.0.1:1337/1'
        false     | null         | 'SuperSecurePassword' || 'redis://default:SuperSecurePassword@127.0.0.1:1337/1'
        false     | 'fulminazzo' | null                  || 'redis://fulminazzo:@127.0.0.1:1337/1'
        false     | 'fulminazzo' | 'SuperSecurePassword' || 'redis://fulminazzo:SuperSecurePassword@127.0.0.1:1337/1'
        true      | null         | null                  || 'rediss://127.0.0.1:1337/1'
        true      | null         | 'SuperSecurePassword' || 'rediss://default:SuperSecurePassword@127.0.0.1:1337/1'
        true      | 'fulminazzo' | null                  || 'rediss://fulminazzo:@127.0.0.1:1337/1'
        true      | 'fulminazzo' | 'SuperSecurePassword' || 'rediss://fulminazzo:SuperSecurePassword@127.0.0.1:1337/1'
    }

}
