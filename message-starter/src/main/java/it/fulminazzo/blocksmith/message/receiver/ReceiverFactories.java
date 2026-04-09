package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry containing all the known {@link ReceiverFactory}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReceiverFactories {
    private static final @NotNull LinkedList<ReceiverFactory> factories;

    static {
        factories = new LinkedList<>();
        factories.add(new ReceiverFactory() {

            @Override
            public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
                return this;
            }

            @Override
            public @NotNull Collection<Receiver> getAllReceivers() {
                return Collections.emptyList();
            }

            @Override
            public @NotNull <R> Receiver create(final @NotNull R receiver) {
                return (Receiver) receiver;
            }

            @Override
            public boolean supports(final @NotNull Class<?> receiverType) {
                return Receiver.class.isAssignableFrom(receiverType);
            }

        });
        ServiceLoader.load(ReceiverFactory.class, ReceiverFactory.class.getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .forEach(factories::add);
    }

    /**
     * Gets all receivers across all the factories.
     *
     * @return all the receivers
     */
    public static @NotNull Collection<Receiver> getAllReceivers() {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return factories.stream()
                .map(ReceiverFactory::getAllReceivers)
                .flatMap(Collection::stream)
                .filter(r -> seen.add(r.getInternal()))
                .collect(Collectors.toSet());
    }

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
     * @param application  the application requesting the factory
     * @return the receiver factory
     */
    public static @NotNull ReceiverFactory get(final @NotNull Class<?> receiverType,
                                               final @NotNull ServerApplication application) {
        for (final ReceiverFactory factory : factories)
            if (factory.setup(application).supports(receiverType)) {
                return factory;
            }
        throw new IllegalArgumentException(String.format("Could not find a %s for receiver type %s.",
                ReceiverFactory.class.getSimpleName(), receiverType.getCanonicalName()
        ));
    }

}
