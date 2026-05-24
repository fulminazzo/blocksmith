package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;

/**
 * Mock {@link MessageBrokerBuilder} for testing purposes.
 *
 * @see MockMessageBroker
 */
public final class MockMessageBrokerBuilder
        extends AbstractMessageBrokerBuilder<MockMessageBroker, MockMessageBrokerBuilder> {

    @Override
    public @NotNull MockMessageBroker build() {
        return new MockMessageBroker(mapper);
    }

}
