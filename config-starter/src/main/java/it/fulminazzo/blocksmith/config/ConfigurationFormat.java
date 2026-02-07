package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.function.Function;

/**
 * Identifies the type of data format language to utilize
 * for the {@link BaseConfigurationAdapter}.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigurationFormat {
    JSON(JsonConfigurationAdapter::new, "json"),
    PROPERTIES(PropertiesConfigurationAdapter::new, "properties"),
    TOML(TomlConfigurationAdapter::new, "toml"),
    XML(XmlConfigurationAdapter::new, "xml"),
    YAML(YamlConfigurationAdapter::new, "yml");

    @NotNull Function<Logger, BaseConfigurationAdapter> adapterSupplier;
    @NotNull String fileExtension;

    /**
     * Gets the adapter for the corresponding format.
     *
     * @param logger the logger
     * @return the adapter
     */
    @NotNull BaseConfigurationAdapter newAdapter(final @NotNull Logger logger) {
        return adapterSupplier.apply(logger);
    }

    /**
     * Gets the corresponding file with the extension of the current format.
     *
     * @param parentDir the parent folder
     * @param fileName  the file name
     * @return the file
     */
    @NotNull File getFile(final @NotNull File parentDir,
                          final @NotNull String fileName) {
        return new File(parentDir, fileName + "." + fileExtension);
    }

}
