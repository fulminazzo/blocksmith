package it.fulminazzo.blocksmith.message.receiver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Registry containing all the known {@link ReceiverFactory}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiverFactories {
    private static final @NotNull LinkedList<ReceiverFactory> factories = ServiceLoader
            .load(ReceiverFactory.class, ReceiverFactory.class.getClassLoader()).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toCollection(LinkedList::new));

    /**
     * Registers a new custom Receiver factory.
     *
     * @param factory the factory
     */
    public static void registerCustomFactory(final @NotNull ReceiverFactory factory) {
        factories.addFirst(factory);
    }

    /**
     * Gets the most appropriate Receiver factory from the given type.
     * <br>
     * Throws {@link IllegalArgumentException} if none found.
     *
     * @param receiverType the receiver type
     * @return the receiver factory
     */
    public static @NotNull ReceiverFactory get(final @NotNull Class<?> receiverType) {
        for (final ReceiverFactory factory : factories)
            if (factory.supports(receiverType)) {
                return factory;
            }
        throw new IllegalArgumentException(String.format("Could not find a %s for receiver type %s.",
                ReceiverFactory.class.getSimpleName(), receiverType.getCanonicalName()
        ));
    }

}
