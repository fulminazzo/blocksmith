package it.fulminazzo.blocksmith.data.mapper;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import org.jetbrains.annotations.NotNull;

/**
 * Contains all the default {@link Mapper}s.
 */
public enum MapperFormat {
    /**
     * A mapper that requires the object to be {@link java.io.Serializable}.
     */
    SERIALIZABLE,
    /**
     * Uses JSON for serialization.
     */
    JSON;

    /**
     * Creates a new mapper.
     *
     * @return the mapper
     */
    @SuppressWarnings("unchecked")
    public @NotNull Mapper newMapper() {
        String type = name().toLowerCase();
        type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
        Class<Mapper> mapperClass = Mapper.class;
        String simpleName = mapperClass.getSimpleName();
        String className = mapperClass.getPackageName() + "." + type + simpleName;
        try {
            Class<Mapper> clazz = (Class<Mapper>) Class.forName(className);
            return Reflect.on(clazz).init().get();
        } catch (ClassNotFoundException e) {
            String moduleName = String.format("%s.%s:%s-%s",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME,
                    type.toLowerCase()
            );
            throw new IllegalStateException(
                    String.format("Could not find suitable %s for %s. ", Mapper.class.getSimpleName(), type) +
                            String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
    }

}
