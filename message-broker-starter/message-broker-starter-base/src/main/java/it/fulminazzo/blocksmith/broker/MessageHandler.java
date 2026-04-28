package it.fulminazzo.blocksmith.broker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MessageHandler {

    @Nullable String handle(final @NotNull String message);

}
