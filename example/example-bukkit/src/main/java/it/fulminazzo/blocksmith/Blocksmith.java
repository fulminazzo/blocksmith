package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.command.HelloCommand;
import it.fulminazzo.blocksmith.message.Messenger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

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
        getCommand("hello").setExecutor(new HelloCommand(this));

        logger.info("{} successfully enabled", getDescription().getName());
    }

    @Override
    public void onDisable() {
        logger.info("{} disabled. Goodbye!", getDescription().getName());
    }

}
