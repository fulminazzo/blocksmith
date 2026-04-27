package it.fulminazzo.blocksmith.message.argument.time.node

import spock.lang.Specification

class TimeUnitTest extends Specification {

    def 'test that #unit formats time to #expected'() {
        when:
        def actual = unit.formatTime(58060800000)

        then:
        actual == expected

        where:
        unit                          || expected
        ArgumentNode.TimeUnit.MILLIS  || 58060800000
        ArgumentNode.TimeUnit.SECONDS || 58060800
        ArgumentNode.TimeUnit.MINUTES || 967680
        ArgumentNode.TimeUnit.HOURS   || 16128
        ArgumentNode.TimeUnit.DAYS    || 672
        ArgumentNode.TimeUnit.WEEKS   || 96
        ArgumentNode.TimeUnit.MONTHS  || 24
        ArgumentNode.TimeUnit.YEARS   || 2
    }

    def 'test that TimeUnit of #name returns #expected'() {
        when:
        def actual = ArgumentNode.TimeUnit.of(name)

        then:
        actual == expected

        where:
        name      || expected
        'millis'  || ArgumentNode.TimeUnit.MILLIS
        'Millis'  || null
        'seconds' || ArgumentNode.TimeUnit.SECONDS
        'Seconds' || null
        'minutes' || ArgumentNode.TimeUnit.MINUTES
        'Minutes' || null
        'hours'   || ArgumentNode.TimeUnit.HOURS
        'Hours'   || null
        'days'    || ArgumentNode.TimeUnit.DAYS
        'Days'    || null
        'weeks'   || ArgumentNode.TimeUnit.WEEKS
        'Weeks'   || null
        'months'  || ArgumentNode.TimeUnit.MONTHS
        'Months'  || null
        'years'   || ArgumentNode.TimeUnit.YEARS
        'Years'   || null
    }

}
