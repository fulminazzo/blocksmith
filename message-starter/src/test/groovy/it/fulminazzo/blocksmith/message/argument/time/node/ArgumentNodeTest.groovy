package it.fulminazzo.blocksmith.message.argument.time.node

import spock.lang.Specification

class ArgumentNodeTest extends Specification {

    def 'test that parseSingle of #unit, #time and #optional returns #expected'() {
        given:
        def node = new ArgumentNode(
                '%unit% %name%',
                unit,
                unit.name.dropRight(1),
                unit.name
        )
        node.optional = optional

        when:
        def actual = node.parseSingle(time)

        then:
        actual == expected

        where:
        unit                          | time                                  | optional || expected
        // MILLIS
        // non-optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | false    || '0 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | false    || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | false    || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | false    || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | false    || '0 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | false    || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | false    || '2 millis'
        // optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | true      | ''
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | true     || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | true     || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | true     || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | true      | ''
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | true     || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | true     || '2 millis'
        // SECONDS
        // non-optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | false    || '0 seconds'
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | false    || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | false    || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | false    || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | false    || '0 seconds'
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | false    || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | false    || '2 seconds'
        // optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | true      | ''
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | true     || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | true     || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | true     || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | true      | ''
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | true     || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | true     || '2 seconds'
        // MINUTES
        // non-optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | false    || '0 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | false    || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | false    || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | false    || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | false    || '0 minutes'
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | false    || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | false    || '2 minutes'
        // optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | true      | ''
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | true     || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | true     || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | true     || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | true      | ''
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | true     || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | true     || '2 minutes'
        // HOURS
        // non-optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | false    || '0 hours'
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | false    || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | false    || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | false    || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | false    || '0 hours'
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | false    || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | false    || '2 hours'
        // optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | true      | ''
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | true     || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | true     || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | true     || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | true      | ''
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | true     || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | true     || '2 hours'
        // DAYS
        // non-optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | false    || '0 days'
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | false    || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | false    || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | false    || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | false    || '0 days'
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | false    || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | false    || '2 days'
        // optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | true      | ''
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | true     || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | true     || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | true     || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | true      | ''
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | true     || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | true     || '2 days'
        // WEEKS
        // non-optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | false    || '0 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | false    || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | false    || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | false    || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | false    || '0 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | false    || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | false    || '2 weeks'
        // optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | true      | ''
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | true     || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | true     || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | true     || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | true      | ''
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | true     || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | true     || '2 weeks'
        // MONTHS
        // non-optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | false    || '0 months'
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | false    || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | false    || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | false    || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false    || '0 months'
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | false    || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | false    || '2 months'
        // optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | true      | ''
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | true     || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | true     || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | true     || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true      | ''
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | true     || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | true     || '2 months'
        // YEARS
        // non-optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | false    || '0 years'
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false    || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false    || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false    || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false    || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false    || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false    || '6 years'
        // optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | true      | ''
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true     || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true     || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true     || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true     || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true     || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true     || '6 years'
    }

}
