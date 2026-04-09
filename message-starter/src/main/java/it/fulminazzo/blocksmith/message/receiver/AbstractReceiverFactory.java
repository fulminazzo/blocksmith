package it.fulminazzo.blocksmith.message.receiver;

import it.fulminazzo.blocksmith.ServerApplication;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Abstract implementation of {@link ReceiverFactory}.
 * Automatically checks if {@link #setup(ServerApplication)} has been invoked
 * before calling any other method.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
abstract class AbstractReceiverFactory implements ReceiverFactory {
    private @NotNull State state = State.UNINITIALIZED;

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        state = State.SETUP;
        return this;
    }

    @Override
    public final @NotNull Collection<Receiver> getAllReceivers() {
        if (isInitialized()) return getAllReceiversImpl();
        else throw notInitializedException();
    }

    /**
     * Gets all the receivers of the platform.
     * In Minecraft servers it will include all the players and the console.
     * <br>
     * <b>WARNING</b>: will not check if the factory has been setup correctly.
     *
     * @return the receivers
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    protected abstract @NotNull Collection<Receiver> getAllReceiversImpl();

    @Override
    public final @NotNull <R> Receiver create(final @NotNull R receiver) {
        if (isInitialized()) return createImpl(receiver);
        else throw notInitializedException();
    }

    /**
     * Creates a new Receiver from the given one.
     * <br>
     * <b>WARNING</b>: will not check if the factory has been setup correctly.
     *
     * @param <R>      the type of the receiver
     * @param receiver the receiver
     * @return the blocksmith receiver
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    protected abstract <R> @NotNull Receiver createImpl(final @NotNull R receiver);

    @Override
    public final boolean supports(final @NotNull Class<?> receiverType) {
        if (isInitialized()) return supportsImpl(receiverType);
        else throw notInitializedException();
    }

    /**
     * Checks if the current factory supports the given receiver type.
     * <br>
     * <b>WARNING</b>: will not check if the factory has been setup correctly.
     *
     * @param receiverType the receiver type
     * @return <code>true</code> if it does
     * @deprecated FOR INTERNAL USE ONLY
     */
    @Deprecated
    protected abstract boolean supportsImpl(final @NotNull Class<?> receiverType);

    private @NotNull IllegalStateException notInitializedException() {
        return new IllegalStateException("ReceiverFactory has not been initialized yet. " +
                String.format("Please use %s#setup(%s) before calling any method",
                        ReceiverFactory.class.getSimpleName(), ServerApplication.class.getSimpleName()
                )
        );
    }

    private boolean isInitialized() {
        return state == State.SETUP;
    }

    /**
     * Identifies the current state of the factory.
     */
    enum State {
        /**
         * {@link AbstractReceiverFactory#setup(ServerApplication)} has not been called yet.
         */
        UNINITIALIZED,
        /**
         * {@link AbstractReceiverFactory#setup(ServerApplication)} has been called.
         */
        SETUP
    }

}
