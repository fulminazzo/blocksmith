package it.fulminazzo.blocksmith.message.receiver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiverFactories {
    private static final @NotNull List<ReceiverFactory> factories = ServiceLoader.load(ReceiverFactory.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList();

    public static @NotNull ReceiverFactory get(final @NotNull Class<?> receiverType) {
        return factories.stream()
                .filter(f -> f.supports(receiverType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a %s for receiver type %s.",
                        ReceiverFactory.class.getSimpleName(), receiverType.getCanonicalName()
                )));
    }

}
