package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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

}
