package it.fulminazzo.blocksmith.message.receiver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiverFactories {
    private static final @NotNull List<ReceiverFactory> factories = ServiceLoader
            .load(ReceiverFactory.class, ReceiverFactory.class.getClassLoader()).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());

    public static @NotNull ReceiverFactory get(final @NotNull Class<?> receiverType) {
        return factories.stream()
                .filter(f -> f.supports(receiverType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a %s for receiver type %s.",
                        ReceiverFactory.class.getSimpleName(), receiverType.getCanonicalName()
                )));
    }

}
