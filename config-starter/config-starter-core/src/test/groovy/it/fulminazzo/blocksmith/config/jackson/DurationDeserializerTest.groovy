package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.Logger
import spock.lang.Specification

import java.time.Duration

class DurationDeserializerTest extends Specification {

    private Logger logger
    private ObjectMapper mapper

    void setup() {
        logger = Mock()
        def module = new SimpleModule()
        module.addDeserializer(Duration, new DurationDeserializer(logger))
        mapper = new ObjectMapper().registerModule(module)
    }

    def 'test that deserialize works'() {
        given:
        def data = '"1y 2Y 2M ABCM 3d 33e  4h 10i 5m 6s        7ms 8ns"'

        and:
        def expected = Duration.ofDays(3 * DurationDeserializer.daysInYear)
                .plusDays(2 * DurationDeserializer.daysInMonth)
                .plusDays(3)
                .plusHours(4)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)

        when:
        def value = mapper.readValue(data, Duration)

        then:
        noExceptionThrown()

        and:
        value == expected

        and:
        1 * logger.warn('Invalid time value \'{}\' for unit {} (path: {})', 'ABC', 'M', '')

        and:
        1 * logger.warn('Unrecognized time notation \'{}\'. Supported units: {} (path: {})', '33e', DurationDeserializer.supportedUnits, '')

        and:
        1 * logger.warn('Unrecognized time notation \'{}\'. Supported units: {} (path: {})', '10i', DurationDeserializer.supportedUnits, '')
    }

    def 'test that deserialize of null works'() {
        given:
        def data = 'null'

        when:
        def value = mapper.readValue(data, Duration)

        then:
        value == null
    }

    def 'test that deserialize of invalid '() {
        given:
        def data = '"33e"'

        when:
        mapper.readValue(data, Duration)

        then:
        def e = thrown(InvalidFormatException)
        e.value == '33e'
    }

}
