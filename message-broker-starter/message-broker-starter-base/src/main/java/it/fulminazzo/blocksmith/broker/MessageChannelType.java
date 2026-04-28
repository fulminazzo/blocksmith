package it.fulminazzo.blocksmith.broker;

/**
 * Defines the type of the message channel.
 */
public enum MessageChannelType {
    /**
     * Will send the message to only the clients listening to that specific channel.
     * Requires a subnamespace to be specified.
     */
    DIRECT,
    /**
     * Will send the message to all the clients listening.
     */
    BROADCAST


}
