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
     * Adds a new migration logic for the specified version.
     *
     * @param version the version at which the configuration will be updated
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
