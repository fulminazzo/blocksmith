package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.command.HelloCommand;
import it.fulminazzo.blocksmith.command.ReloadCommand;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.provider.MessageProvider;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class Blocksmith extends JavaPlugin {
    private final @NotNull Logger logger;

    @Getter
    private final @NotNull Messenger messenger;

    public Blocksmith() {
        try {
            Constructor<JDK14LoggerAdapter> constructor = JDK14LoggerAdapter.class.getDeclaredConstructor(java.util.logging.Logger.class);
            constructor.setAccessible(true);
            this.logger = constructor.newInstance(getLogger());
            this.messenger = new Messenger(logger);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Could not instantiate SLF4J logger", e);
        }
    }

    @Override
    public void onEnable() {
        getCommand("blocksmithreload").setExecutor(new ReloadCommand(this));
        getCommand("hello").setExecutor(new HelloCommand(this));

        enable();
    }

    public void enable() {
        try {
            messenger.setMessageProvider(MessageProvider.translation(
                    new File(getDataFolder(), "messages"),
                    "messages",
                    ConfigurationFormat.YAML,
                    logger
            ));

            logger.info("{} successfully enabled", getDescription().getName());
        } catch (IOException e) {
            logger.error("An error occurred while loading the plugin: {}", e.getMessage());
            logger.error("Disabling plugin to avoid further errors...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        disable();
    }

    public void disable() {
        messenger.setMessageProvider(null);

        logger.info("{} disabled. Goodbye!", getDescription().getName());
    }

}
