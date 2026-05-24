package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Mock {@link MessageBroker} for testing purposes.
 *
 * @see MockMessageChannelSettings
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MockMessageBroker extends AbstractMessageBroker<MockMessageChannelSettings> {
    @Getter
    private final @NotNull Mapper mapper;

    @Override
    public @NotNull MessageChannel newChannel(final @NonNull MockMessageChannelSettings settings) {
        throw new UnsupportedOperationException();
    }

}
