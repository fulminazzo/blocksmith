package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.ProjectInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies the supported types of data source configurations.
 */
public enum DataSourceType {
    CACHED,
    MEMORY,
    FILE,
    SQL,
    REDIS,
    MONGO;

    /**
     * Creates a new Data source config for the corresponding type.
     *
     * @return the data source config
     */
    @SuppressWarnings("unchecked")
    public @NotNull Class<DataSourceConfig> getConfigClass() {
        String type = name().toLowerCase();
        type = Character.toUpperCase(type.charAt(0)) + type.substring(1);

        String lowercaseType = type.toLowerCase();
        if (this == CACHED) lowercaseType = "cache";

        String packageName = lowercaseType;
        if (this == MONGO) packageName += "db";

        String className = DataSourceConfig.class.getCanonicalName()
                .replace("config.", packageName + ".config." + type);
        try {
            return (Class<DataSourceConfig>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            String moduleName = String.format("%s.%s:%s-%s",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME.replace("-base", ""),
                    lowercaseType
            );
            throw new IllegalStateException(
                    String.format("Could not find suitable %s for %s. ", DataSourceConfig.class.getSimpleName(), type) +
                            String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
    }

}
