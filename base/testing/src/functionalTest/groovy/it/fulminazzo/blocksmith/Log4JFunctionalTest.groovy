package it.fulminazzo.blocksmith

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class Log4JFunctionalTest extends Specification {

    def 'test that log4j works'() {
        when:
        log.info('Hello, world!')

        then:
        noExceptionThrown()
    }

}
