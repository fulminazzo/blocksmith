package it.fulminazzo.blocksmith.message.argument.time.node

import spock.lang.Specification

class ArgumentNodeTest extends Specification {

    def 'test that parseSingle of #unit, #time, #full and #optional returns #expected'() {
        given:
        def node = new ArgumentNode(
                '%unit% %name%',
                unit,
                unit.name.dropRight(1),
                unit.name,
                full
        )
        node.optional = optional

        when:
        def actual = node.parseSingle(time)

        then:
        actual == expected

        where:
        unit                          | time                                  | full  | optional || expected
        // MILLIS
        // non-full, non-optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | false | false    || '0 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | false | false    || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | false | false    || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | false | false    || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | false | false    || '0 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | false | false    || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | false | false    || '2 millis'
        // non-full, optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | false | true     || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | false | true     || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | false | true     || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | false | true      | ''
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | false | true     || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | false | true     || '2 millis'
        // MILLIS
        // full, non-optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | true  | false    || '0 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | true  | false    || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | true  | false    || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | true  | false    || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | true  | false    || '1000 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | true  | false    || '1001 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | true  | false    || '1002 millis'
        // full, optional
        ArgumentNode.TimeUnit.MILLIS  | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.MILLIS  | 1                                     | true  | true     || '1 milli'
        ArgumentNode.TimeUnit.MILLIS  | 2                                     | true  | true     || '2 millis'
        ArgumentNode.TimeUnit.MILLIS  | 999                                   | true  | true     || '999 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1000L                                 | true  | true      | '1000 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1001                                  | true  | true     || '1001 millis'
        ArgumentNode.TimeUnit.MILLIS  | 1002                                  | true  | true     || '1002 millis'
        // SECONDS
        // non-full, non-optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | false | false    || '0 seconds'
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | false | false    || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | false | false    || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | false | false    || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | false | false    || '0 seconds'
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | false | false    || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | false | false    || '2 seconds'
        // non-full, optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | false | true     || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | false | true     || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | false | true     || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | false | true      | ''
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | false | true     || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | false | true     || '2 seconds'
        // full, non-optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | true  | false    || '0 seconds'
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | true  | false    || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | true  | false    || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | true  | false    || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | true  | false    || '60 seconds'
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | true  | false    || '61 seconds'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | true  | false    || '62 seconds'
        // full, optional
        ArgumentNode.TimeUnit.SECONDS | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.SECONDS | 1000L                                 | true  | true     || '1 second'
        ArgumentNode.TimeUnit.SECONDS | 2 * 1000L                             | true  | true     || '2 seconds'
        ArgumentNode.TimeUnit.SECONDS | 59 * 1000L                            | true  | true     || '59 seconds'
        ArgumentNode.TimeUnit.SECONDS | 60 * 1000L                            | true  | true      | '60 seconds'
        ArgumentNode.TimeUnit.SECONDS | 61 * 1000L                            | true  | true     || '61 seconds'
        ArgumentNode.TimeUnit.SECONDS | 62 * 1000L                            | true  | true     || '62 seconds'
        // MINUTES
        // non-full, non-optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | false | false    || '0 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | false | false    || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | false | false    || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | false | false    || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | false | false    || '0 minutes'
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | false | false    || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | false | false    || '2 minutes'
        // non-full, optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | false | true     || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | false | true     || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | false | true     || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | false | true      | ''
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | false | true     || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | false | true     || '2 minutes'
        // full, non-optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | true  | false    || '0 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | true  | false    || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | true  | false    || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | true  | false    || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | true  | false    || '60 minutes'
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | true  | false    || '61 minutes'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | true  | false    || '62 minutes'
        // full, optional
        ArgumentNode.TimeUnit.MINUTES | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.MINUTES | 60 * 1000L                            | true  | true     || '1 minute'
        ArgumentNode.TimeUnit.MINUTES | 2 * 60 * 1000L                        | true  | true     || '2 minutes'
        ArgumentNode.TimeUnit.MINUTES | 59 * 60 * 1000L                       | true  | true     || '59 minutes'
        ArgumentNode.TimeUnit.MINUTES | 60 * 60 * 1000L                       | true  | true      | '60 minutes'
        ArgumentNode.TimeUnit.MINUTES | 61 * 60 * 1000L                       | true  | true     || '61 minutes'
        ArgumentNode.TimeUnit.MINUTES | 62 * 60 * 1000L                       | true  | true     || '62 minutes'
        // HOURS
        // non-full, non-optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | false | false    || '0 hours'
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | false | false    || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | false | false    || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | false | false    || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | false | false    || '0 hours'
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | false | false    || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | false | false    || '2 hours'
        // non-full, optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | false | true     || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | false | true     || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | false | true     || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | false | true      | ''
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | false | true     || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | false | true     || '2 hours'
        // full, non-optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | true  | false    || '0 hours'
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | true  | false    || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | true  | false    || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | true  | false    || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | true  | false    || '24 hours'
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | true  | false    || '25 hours'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | true  | false    || '26 hours'
        // full, optional
        ArgumentNode.TimeUnit.HOURS   | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.HOURS   | 60 * 60 * 1000L                       | true  | true     || '1 hour'
        ArgumentNode.TimeUnit.HOURS   | 2 * 60 * 60 * 1000L                   | true  | true     || '2 hours'
        ArgumentNode.TimeUnit.HOURS   | 23 * 60 * 60 * 1000L                  | true  | true     || '23 hours'
        ArgumentNode.TimeUnit.HOURS   | 24 * 60 * 60 * 1000L                  | true  | true      | '24 hours'
        ArgumentNode.TimeUnit.HOURS   | 25 * 60 * 60 * 1000L                  | true  | true     || '25 hours'
        ArgumentNode.TimeUnit.HOURS   | 26 * 60 * 60 * 1000L                  | true  | true     || '26 hours'
        // DAYS
        // non-full, non-optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | false | false    || '0 days'
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | false | false    || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | false | false    || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | false | false    || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | false | false    || '0 days'
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | false | false    || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | false | false    || '2 days'
        // non-full, optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | false | true     || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | false | true     || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | false | true     || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | false | true      | ''
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | false | true     || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | false | true     || '2 days'
        // full, non-optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | true  | false    || '0 days'
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | true  | false    || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | true  | false    || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | true  | false    || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | true  | false    || '7 days'
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | true  | false    || '8 days'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | true  | false    || '9 days'
        // full, optional
        ArgumentNode.TimeUnit.DAYS    | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.DAYS    | 24 * 60 * 60 * 1000L                  | true  | true     || '1 day'
        ArgumentNode.TimeUnit.DAYS    | 2 * 24 * 60 * 60 * 1000L              | true  | true     || '2 days'
        ArgumentNode.TimeUnit.DAYS    | 6 * 24 * 60 * 60 * 1000L              | true  | true     || '6 days'
        ArgumentNode.TimeUnit.DAYS    | 7 * 24 * 60 * 60 * 1000L              | true  | true      | '7 days'
        ArgumentNode.TimeUnit.DAYS    | 8 * 24 * 60 * 60 * 1000L              | true  | true     || '8 days'
        ArgumentNode.TimeUnit.DAYS    | 9 * 24 * 60 * 60 * 1000L              | true  | true     || '9 days'
        // WEEKS
        // non-full, non-optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | false | false    || '0 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | false | false    || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '0 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '2 weeks'
        // non-full, optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | false | true     || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | false | true     || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | false | true     || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | false | true      | ''
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | false | true     || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | false | true     || '2 weeks'
        // full, non-optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | true  | false    || '0 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | true  | false    || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '4 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '5 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '6 weeks'
        // full, optional
        ArgumentNode.TimeUnit.WEEKS   | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.WEEKS   | 7 * 24 * 60 * 60 * 1000L              | true  | true     || '1 week'
        ArgumentNode.TimeUnit.WEEKS   | 2 * 7 * 24 * 60 * 60 * 1000L          | true  | true     || '2 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 3 * 7 * 24 * 60 * 60 * 1000L          | true  | true     || '3 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 4 * 7 * 24 * 60 * 60 * 1000L          | true  | true      | '4 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 5 * 7 * 24 * 60 * 60 * 1000L          | true  | true     || '5 weeks'
        ArgumentNode.TimeUnit.WEEKS   | 6 * 7 * 24 * 60 * 60 * 1000L          | true  | true     || '6 weeks'
        // MONTHS
        // non-full, non-optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | false | false    || '0 months'
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | false | false    || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | false | false    || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | false    || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | false    || '0 months'
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | false    || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | false    || '2 months'
        // non-full, optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | false | true     || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | false | true     || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | true     || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | true      | ''
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | true     || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | true     || '2 months'
        // full, non-optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | true  | false    || '0 months'
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | true  | false    || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | true  | false    || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | false    || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | false    || '12 months'
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | false    || '13 months'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | false    || '14 months'
        // full, optional
        ArgumentNode.TimeUnit.MONTHS  | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.MONTHS  | 4 * 7 * 24 * 60 * 60 * 1000L          | true  | true     || '1 month'
        ArgumentNode.TimeUnit.MONTHS  | 2 * 4 * 7 * 24 * 60 * 60 * 1000L      | true  | true     || '2 months'
        ArgumentNode.TimeUnit.MONTHS  | 11 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | true     || '11 months'
        ArgumentNode.TimeUnit.MONTHS  | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | true      | '12 months'
        ArgumentNode.TimeUnit.MONTHS  | 13 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | true     || '13 months'
        ArgumentNode.TimeUnit.MONTHS  | 14 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | true     || '14 months'
        // YEARS
        // non-full, non-optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | false | false    || '0 years'
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | false    || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | false    || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | false    || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | false    || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | false    || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | false    || '6 years'
        // non-full, optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | false | true      | ''
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | false | true     || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | true     || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | true     || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | true     || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | true     || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | false | true     || '6 years'
        // full, non-optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | true  | false    || '0 years'
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | false    || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | false    || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | false    || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | false    || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | false    || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | false    || '6 years'
        // full, optional
        ArgumentNode.TimeUnit.YEARS   | 0                                     | true  | true      | ''
        ArgumentNode.TimeUnit.YEARS   | 12 * 4 * 7 * 24 * 60 * 60 * 1000L     | true  | true     || '1 year'
        ArgumentNode.TimeUnit.YEARS   | 2 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | true     || '2 years'
        ArgumentNode.TimeUnit.YEARS   | 3 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | true     || '3 years'
        ArgumentNode.TimeUnit.YEARS   | 4 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | true     || '4 years'
        ArgumentNode.TimeUnit.YEARS   | 5 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | true     || '5 years'
        ArgumentNode.TimeUnit.YEARS   | 6 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L | true  | true     || '6 years'
    }

}
