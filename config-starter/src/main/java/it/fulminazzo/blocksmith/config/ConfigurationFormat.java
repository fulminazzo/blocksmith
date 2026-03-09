package it.fulminazzo.blocksmith.config;

import it.fulminazzo.blocksmith.ProjectInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    @SuppressWarnings("unchecked")
    @NotNull BaseConfigurationAdapter newAdapter(final @Nullable Logger logger) {
        String type = name().toLowerCase();
        type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        String className = BaseConfigurationAdapter.class.getCanonicalName()
                .replace("Base", type);
        try {
            Class<BaseConfigurationAdapter> clazz = (Class<BaseConfigurationAdapter>) Class.forName(className);
            Constructor<BaseConfigurationAdapter> constructor = clazz.getDeclaredConstructor(Logger.class);
            constructor.setAccessible(true);
            return constructor.newInstance(logger);
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
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException(cause);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Could not find constructor %s(%s)",
                    className,
                    Logger.class.getCanonicalName()
            ));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Could not instantiate %s", className), e);
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
