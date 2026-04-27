package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification

import java.time.Duration

class DurationSerializerTest extends Specification {

    private ObjectMapper mapper

    void setup() {
        def module = new SimpleModule()
        module.addSerializer(Duration, new DurationSerializer())
        mapper = new ObjectMapper().registerModule(module)
    }

    def 'test that serialize of #data returns #expected'() {
        when:
        def actual = mapper.writeValueAsString(data)
        if (data != null && actual.startsWith('"')) actual = actual.substring(1, actual.length() - 1)

        then:
        actual == expected

        where:
        data                                                   || expected
        Duration.ofSeconds(0)                                  || '0'
        Duration.ofSeconds(1)                                  || '1'
        Duration.ofMillis(500)                                 || '0.5'
        Duration.ofMillis(503)                                 || '0.503'
        Duration.ofMillis(1500)                                || '1.5'
        Duration.ofMillis(1503)                                || '1.503'
        Duration.ofMillis(-500)                                || '-0.5'
        Duration.ofMillis(-503)                                || '-0.503'
        Duration.ofMillis(-1500)                               || '-1.5'
        Duration.ofMillis(-1503)                               || '-1.503'
        Duration.ofSeconds(0)
                .plusNanos(8)                                  || '8ns'
        Duration.ofSeconds(0)
                .plusMillis(7)
                .plusNanos(8)                                  || '7ms 8ns'
        Duration.ofSeconds(0)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '6s 7ms 8ns'
        Duration.ofSeconds(0)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '5m 6s 7ms 8ns'
        Duration.ofSeconds(0)
                .plusHours(4)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '4h 5m 6s 7ms 8ns'
        Duration.ofSeconds(0)
                .plusDays(3)
                .plusHours(4)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '3d 4h 5m 6s 7ms 8ns'
        Duration.ofSeconds(0)
                .plusDays(2 * DurationSerializer.daysInMonth)
                .plusDays(3)
                .plusHours(4)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '2M 3d 4h 5m 6s 7ms 8ns'
        Duration.ofSeconds(0)
                .plusDays(DurationSerializer.daysInYear)
                .plusDays(2 * DurationSerializer.daysInMonth)
                .plusDays(3)
                .plusHours(4)
                .plusMinutes(5)
                .plusSeconds(6)
                .plusMillis(7)
                .plusNanos(8)                                  || '1y 2M 3d 4h 5m 6s 7ms 8ns'
        Duration.ofSeconds(0)
                .minusNanos(8)                                 || '-8ns'
        Duration.ofSeconds(0)
                .minusMillis(7)
                .minusNanos(8)                                 || '-7ms -8ns'
        Duration.ofSeconds(0)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-6s -7ms -8ns'
        Duration.ofSeconds(0)
                .minusMinutes(5)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-5m -6s -7ms -8ns'
        Duration.ofSeconds(0)
                .minusHours(4)
                .minusMinutes(5)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-4h -5m -6s -7ms -8ns'
        Duration.ofSeconds(0)
                .minusDays(3)
                .minusHours(4)
                .minusMinutes(5)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-3d -4h -5m -6s -7ms -8ns'
        Duration.ofSeconds(0)
                .minusDays(2 * DurationSerializer.daysInMonth)
                .minusDays(3)
                .minusHours(4)
                .minusMinutes(5)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-2M -3d -4h -5m -6s -7ms -8ns'
        Duration.ofSeconds(0)
                .minusDays(DurationSerializer.daysInYear)
                .minusDays(2 * DurationSerializer.daysInMonth)
                .minusDays(3)
                .minusHours(4)
                .minusMinutes(5)
                .minusSeconds(6)
                .minusMillis(7)
                .minusNanos(8)                                 || '-1y -2M -3d -4h -5m -6s -7ms -8ns'
    }

}
