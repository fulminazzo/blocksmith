package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.ProjectInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    public @NotNull DataSourceConfig newConfig() {
        String type = name().toLowerCase();
        type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        String lowercaseType = type.toLowerCase();
        if (this == CACHED) lowercaseType = "cache";
        String className = DataSourceConfig.class.getCanonicalName()
                .replace("config", lowercaseType + ".config." + type);
        try {
            Class<DataSourceConfig> clazz = (Class<DataSourceConfig>) Class.forName(className);
            Constructor<DataSourceConfig> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
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
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException(cause);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Could not find constructor %s()", className));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Could not instantiate %s", className), e);
        }
    }

}
