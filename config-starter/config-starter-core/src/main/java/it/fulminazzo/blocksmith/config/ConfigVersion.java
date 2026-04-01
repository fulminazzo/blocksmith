package it.fulminazzo.blocksmith.config;

import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Identifies a configuration version along with migrations to update previous versions.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigVersion {
    @Getter
    private final double version;
    private final @NotNull Map<Double, Function<Migration, Migration>> migrations = new TreeMap<>();

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
     * Instantiates a new Config version.
     *
     * @param version the version
     * @return the config version
     */
    public static @NotNull ConfigVersion of(final double version) {
        return new ConfigVersion(version);
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
