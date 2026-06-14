package it.fulminazzo.blocksmith.broker.plugin.coordinator.velocity

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import spock.lang.Specification

class VelocityChannelUtilsTest extends Specification {

    def 'test that toIdentifier of #channelName returns #expected'() {
        when:
        def actual = VelocityChannelUtils.toIdentifier(channelName)

        then:
        actual == expected

        where:
        channelName    || expected
        'test:channel' || MinecraftChannelIdentifier.create('test', 'channel')
        'channel'      || MinecraftChannelIdentifier.create(VelocityChannelUtils.FALLBACK_NAMESPACE, 'channel')
    }

    def 'test that toChannelName of #identifier returns #expected'() {
        when:
        def actual = VelocityChannelUtils.toChannelName(identifier)

        then:
        actual == expected

        where:
        identifier                                                                            || expected
        MinecraftChannelIdentifier.create('test', 'channel')                                  || 'test:channel'
        MinecraftChannelIdentifier.create(VelocityChannelUtils.FALLBACK_NAMESPACE, 'channel') || 'channel'
    }

}
