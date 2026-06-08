package it.fulminazzo.blocksmith.bungee;

import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.reflect.Reflect;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
public final class Blocksmith extends Plugin {
    private final @NotNull Logger logger = Reflect.on(JDK14LoggerAdapter.class)
            .init(getLogger())
            .get();
    private final @NotNull BlocksmithMain main = new BlocksmithMain(logger);

    @Override
    public void onEnable() {
        main.enable();
    }

    @Override
    public void onDisable() {
        main.disable();
    }

}
