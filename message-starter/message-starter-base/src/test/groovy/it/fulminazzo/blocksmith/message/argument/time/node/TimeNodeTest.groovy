package it.fulminazzo.blocksmith.message.argument.time.node

import spock.lang.Specification

class TimeNodeTest extends Specification {

    def 'test that parse correctly parses time'() {
        given:
        def time = 58060980000L

        and:
        def node = new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.YEARS, 'year', 'years', true)
                .addChild(new ArgumentNode(' %unit% %name%', ArgumentNode.TimeUnit.MONTHS, 'month', 'months', false).setOptional(true))
                .addChild(new LiteralNode(' '))
                .addChild(new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.DAYS, 'day', 'days', false))
                .addChild(new LiteralNode(' this should totally be fine'))
                .addChild(new ArgumentNode(' %unit% %name%', ArgumentNode.TimeUnit.HOURS, 'hour', 'hours', true).setOptional(true))
                .addChild(new LiteralNode(' '))
                .addChild(new ArgumentNode('%unit% %name%', ArgumentNode.TimeUnit.MINUTES, 'minute', 'minutes', false))

        and:
        def expected = '2 years 0 days this should totally be fine 16128 hours 3 minutes'

        when:
        def actual = node.parse(time)

        then:
        actual == expected
    }

}
