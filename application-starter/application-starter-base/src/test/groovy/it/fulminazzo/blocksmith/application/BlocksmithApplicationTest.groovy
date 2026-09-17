package it.fulminazzo.blocksmith.application

import it.fulminazzo.blocksmith.reflect.Reflect
import it.fulminazzo.blocksmith.reflect.ReflectException
import spock.lang.Specification

class BlocksmithApplicationTest extends Specification {

    def 'test that BlocksmithApplication#method calls on delegate'() {
        given:
        def delegate = Mock(Application)

        and:
        def application = new BlocksmithApplication() {
            @Override
            void onStartup() {

            }

            @Override
            void onShutdown() {

            }
        }
        application.delegate = delegate

        when:
        application."$method"()

        then:
        1 * delegate."$method"()

        where:
        method << Application
                .methods
                *.name
                .findAll { it.startsWith('get') }
    }

    def 'test that BlocksmithApplication throws if no delegate is set'() {
        given:
        def application = new BlocksmithApplication() {
            @Override
            void onStartup() {

            }

            @Override
            void onShutdown() {

            }
        }

        when:
        Reflect.on(application).invoke('getDelegate')

        then:
        def e = thrown(ReflectException)
        (e.cause instanceof IllegalStateException)
    }

}
