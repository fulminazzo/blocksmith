package it.fulminazzo.blocksmith.bukkit;

import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
public final class Blocksmith extends JavaPlugin {
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
