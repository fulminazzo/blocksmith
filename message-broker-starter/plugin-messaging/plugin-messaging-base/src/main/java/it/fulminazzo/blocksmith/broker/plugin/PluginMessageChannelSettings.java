package it.fulminazzo.blocksmith.broker.plugin;

import it.fulminazzo.blocksmith.broker.MessageChannelSettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Message channel settings for Plugin channels.
 *
 * @see PluginMessageChannel
 * @see PluginMessageBroker
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class PluginMessageChannelSettings extends MessageChannelSettings<PluginMessageChannelSettings> {

}
