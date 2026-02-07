package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * Identifies the type of data format language to utilize
 * for the {@link BaseConfigurationAdapter}.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigurationFormat {
    JSON(JsonConfigurationAdapter::new),
    PROPERTIES(PropertiesConfigurationAdapter::new),
    TOML(TomlConfigurationAdapter::new),
    XML(XmlConfigurationAdapter::new),
    YAML(YamlConfigurationAdapter::new);

    @NotNull Function<Logger, BaseConfigurationAdapter> adapterSupplier;

    /**
     * Gets the adapter for the corresponding format.
     *
     * @param logger the logger
     * @return the adapter
     */
    @NotNull BaseConfigurationAdapter newAdapter(final @NotNull Logger logger) {
        return adapterSupplier.apply(logger);
    }

}
