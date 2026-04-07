package it.fulminazzo.blocksmith.config;

import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Identifies a configuration version along with migrations to update previous versions.
 * <br>
 * Configuration objects can be specified with a <code>static</code> field that
 * represents their current version.
 * Each migration must specify the version it refers to.
 * <br>
 * Example:
 * <pre>{@code
 * @Data
 * @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
 * public class Configuration {
 *
 *     private static final ConfigVersion version = ConfigVersion.of(3.0) // current version is 3.0
 *             .migrate(2.0, m -> m // version 2.0 migrations from 1.0 (default base version)
 *                     .add("port", 8080)
 *                     .rename("hostname", "host")
 *             )
 *             .migrate(3.0, m -> m // version 3.0 migrations from 2.0
 *                     .add("timeoutSeconds", 60 * 5)
 *                     .remove("ssl")
 *             );
 *
 *     String hostname;
 *
 *     int port = 8080;
 *
 *     int timeoutSeconds = 60 * 5;
 *
 * }
 * }</pre>
 */
@Value(staticConstructor = "of")
public class ConfigVersion {
    /**
     * The name of the property associated with the configuration version.
     */
    public static final @NotNull String PROPERTY_NAME = "version";

    @Getter
    double version;
    @NotNull Map<Double, Function<Migration, Migration>> migrations = new TreeMap<>();

    /**
     * Applies the migrations for the specified version to the data.
     *
     * @param currentVersion the current version of the data
     * @param data           the data
     * @return the updated data
     */
    public @NotNull Map<String, Object> applyMigrations(final double currentVersion, @NotNull Map<String, Object> data) {
        for (double v : migrations.keySet()) {
            if (v <= currentVersion) continue;
            Function<Migration, Migration> migration = migrations.get(v);
            data = migration.apply(new Migration(data)).getData();
        }
        return data;
    }

    /**
     * Adds a new migration logic for the specified version.
     *
     * @param version   the version at which the configuration will be updated
     * @param migration the migration logic
     * @return this object (for method chaining)
     */
    public @NotNull ConfigVersion migrate(final double version, final @NotNull Function<Migration, Migration> migration) {
        if (migrations.containsKey(version))
            throw new IllegalArgumentException("Migration already present for version " + version);
        migrations.put(version, migration);
        return this;
    }

    /**
     * Gets the version of a general type.
     * Basically looks up for a static field of type {@link ConfigVersion}.
     *
     * @param type the type
     * @return the version (if found)
     */
    public static @NotNull Optional<ConfigVersion> getVersion(final @NotNull Class<?> type) {
        Reflect reflect = Reflect.on(type);
        return reflect.getFields(f -> Modifier.isStatic(f.getModifiers()) && f.getType().equals(ConfigVersion.class))
                .stream()
                .findFirst()
                .map(f -> reflect.get(f).get());
    }

}
