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
     * Checks if the given file is of the current configuration format type.
     *
     * @param file the file
     * @return <code>true</code> if it is
     */
    public boolean isValidFile(final @NotNull File file) {
        return isValidFile(file.getName());
    }

    /**
     * Checks if the given file is of the current configuration format type.
     *
     * @param fileName the file name
     * @return <code>true</code> if it is
     */
    public boolean isValidFile(final @NotNull String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (extension.equals("yaml")) extension = "yml";
        return extension.equals(fileExtension);
    }

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

    /**
     * Tries to obtain the best {@link ConfigurationFormat} from the given file extension.
     *
     * @param file the file
     * @return the configuration format
     */
    public static @NotNull ConfigurationFormat fromFile(final @NotNull File file) {
        return fromFile(file.getName());
    }

    /**
     * Tries to obtain the best {@link ConfigurationFormat} from the given file extension.
     *
     * @param fileName the file name
     * @return the configuration format
     */
    private static @NotNull ConfigurationFormat fromFile(final @NotNull String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (extension.equals("yaml")) extension = "yml";
        for (ConfigurationFormat format : ConfigurationFormat.values())
            if (format.fileExtension.equals(extension)) return format;
        throw new IllegalArgumentException("Could not find configuration format from file: " + fileName);
    }

}
