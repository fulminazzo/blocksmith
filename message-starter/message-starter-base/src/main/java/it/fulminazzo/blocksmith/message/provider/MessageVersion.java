package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.util.MapUtils;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

@Value(staticConstructor = "of")
public class MessageVersion {
    /**
     * The name of the property associated with the message configuration version.
     */
    public static final @NotNull String PROPERTY_NAME = "version";

    @Getter
    double version;
    @NotNull Map<Double, Function<Migration, Migration>> migrations = new TreeMap<>();

    /**
     * Applies the migrations for the specified version to the messages.
     *
     * @param currentVersion the current version of the messages
     * @param messages       the messages
     * @param reference      the reference to use to update messages
     * @return the updated messages
     */
    public @NotNull Map<String, Object> applyMigrations(final double currentVersion,
                                                        @NotNull Map<String, Object> messages,
                                                        @NotNull Map<String, Object> reference) {
        messages = MapUtils.flatten(messages);
        reference = MapUtils.flatten(reference);
        for (double v : migrations.keySet()) {
            if (v <= currentVersion) continue;
            Function<Migration, Migration> migration = migrations.get(v);
            messages = migration.apply(new Migration(messages, reference)).getData();
        }
        return MapUtils.unflatten(messages);
    }

    /**
     * Adds a new migration logic for the specified version.
     *
     * @param version   the version at which the configuration will be updated
     * @param migration the migration logic
     * @return this object (for method chaining)
     */
    public @NotNull MessageVersion migrate(final double version, final @NotNull Function<Migration, Migration> migration) {
        if (migrations.containsKey(version))
            throw new IllegalArgumentException("Migration already present for version " + version);
        migrations.put(version, migration);
        return this;
    }

}
