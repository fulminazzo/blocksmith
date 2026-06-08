package it.fulminazzo.blocksmith.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import it.fulminazzo.blocksmith.BlocksmithMain;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
@SuppressWarnings("checkstyle:MissingJavadocMethod") // for events
public final class Blocksmith {
    private final @NotNull BlocksmithMain main;

    /**
     * Instantiates this class.
     *
     * @param logger the logger
     */
    @Inject
    public Blocksmith(final @NotNull Logger logger) {
        this.main = new BlocksmithMain(logger);
    }

    @Subscribe
    public void onEnable(final @NotNull ProxyInitializeEvent event) {
        main.enable();
    }

    @Subscribe
    public void onDisable(final @NotNull ProxyInitializeEvent event) {
        main.disable();
    }

}
