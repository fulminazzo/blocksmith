package it.fulminazzo.blocksmith.broker

import spock.lang.Specification

class MessageChannelSettingsTest extends Specification {
    private final MessageChannelSettings settings = new MockMessageChannelSettings()

    def 'test that getChannelName throws only if not specified'() {
        when:
        settings.channelName

        then:
        thrown(NullPointerException)

        when:
        def channelName = settings.withChannelName('test').channelName

        then:
        noExceptionThrown()

        and:
        channelName == 'test'
    }

    def 'test that broadcast sets correct channel type'() {
        when:
        settings.channelType

        then:
        thrown(NullPointerException)

        when:
        def channelType = settings.broadcast().channelType

        then:
        channelType == MessageChannelType.BROADCAST
    }

    def 'test that direct sets correct channel type and subchannel name'() {
        when:
        settings.channelType

        then:
        thrown(NullPointerException)

        when:
        settings.subchannelName

        then:
        thrown(NullPointerException)

        when:
        def channelType = settings.direct('test').channelType

        then:
        channelType == MessageChannelType.DIRECT

        when:
        def subchannelName = settings.subchannelName

        then:
        subchannelName == 'test'
    }

}
