package it.fulminazzo.blocksmith.config;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.bval.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Identifies the type of data format language to utilize
 * for the {@link BaseConfigurationAdapter}.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigurationFormat {
    JSON("json"),
    PROPERTIES("properties"),
    TOML("toml"),
    XML("xml"),
    YAML("yml");

    @NotNull String fileExtension;

    /**
     * Gets the adapter for the corresponding format.
     *
     * @param logger the logger
     * @return the adapter
     */
    @NotNull BaseConfigurationAdapter newAdapter(final @NotNull Logger logger) {
        final String type = StringUtils.capitalize(name());
        try {
            String className = BaseConfigurationAdapter.class.getCanonicalName()
                    .replace("Base", type);
            Class<?> clazz = Class.forName(className);
            return (BaseConfigurationAdapter) ReflectionUtils.initialize(
                    clazz,
                    List.of(Logger.class),
                    logger
            );
        } catch (ClassNotFoundException e) {
            String moduleName = String.format("%s.%s:%s-%s",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME,
                    type.toLowerCase()
            );
            throw new IllegalStateException(
                    String.format("Could not find suitable %s for %s. ", ConfigurationAdapter.class.getSimpleName(), type) +
                            String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
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
