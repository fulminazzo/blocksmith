package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.command.HelloCommand;
import it.fulminazzo.blocksmith.command.ReloadCommand;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.file.FileDataSource;
import it.fulminazzo.blocksmith.data.file.FileRepositorySettings;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.message.provider.MessageProvider;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

public final class Blocksmith extends JavaPlugin {
    private final @NotNull Logger logger;

    @Getter
    private final @NotNull Messenger messenger;

    private @Nullable RepositoryDataSource<?> dataSource;
    private @Nullable Repository<BlocksmithUser, UUID> repository;

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
        getCommand(getName().toLowerCase() + "reload").setExecutor(new ReloadCommand(this));
        getCommand("hello").setExecutor(new HelloCommand(this));

        enable();
    }

    @SuppressWarnings("unchecked")
    public void enable() {
        try {
            logger.info("Loading translation messages.");
            messenger.setMessageProvider(MessageProvider.translation(
                    getDataFolder(),
                    "messages",
                    ConfigurationFormat.YAML,
                    logger
            ));

            logger.info("Loading player data.");
            dataSource = FileDataSource.create(Executors.newCachedThreadPool());
            repository = ((RepositoryDataSource<FileRepositorySettings>) dataSource).newRepository(
                    BlocksmithUser.class,
                    new FileRepositorySettings()
                            .withDataDirectory(new File(getDataFolder(), "users"))
                            .withFormat(ConfigurationFormat.JSON)
                            .withLogger(logger)
            );

            logger.info("{} v{} successfully enabled", getName(), getDescription().getVersion());
        } catch (IOException e) {
            logger.error("An error occurred while loading the plugin: {}", e.getMessage());
            logger.error("Disabling plugin to avoid further errors...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            disable();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disable() throws IOException {
        messenger.setMessageProvider(null);

        repository = null;

        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }

        logger.info("{} disabled. Goodbye!", getDescription().getName());
    }

    public @NotNull Repository<BlocksmithUser, UUID> getRepository() {
        return Objects.requireNonNull(repository, "repository has not been loaded yet");
    }

}
