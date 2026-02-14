package it.fulminazzo.blocksmith.data.mapper;

import it.fulminazzo.blocksmith.ProjectInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {
    public static final @NotNull Mapper SERIALIZABLE = getMapper("Serializable");
    public static final @NotNull Mapper JSON = getMapper("Json");

    @SuppressWarnings("unchecked")
    static @NotNull Mapper getMapper(final @NotNull String prefix) {
        String className = Mapper.class.getPackageName() + "." + prefix + Mapper.class.getSimpleName();
        try {
            Class<Mapper> clazz = (Class<Mapper>) Class.forName(className);
            Constructor<Mapper> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            return new MissingMapper(prefix);
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

    private static final class MissingMapper implements Mapper {
        private final @NotNull String prefix;

        private MissingMapper(final @NotNull String prefix) {
            this.prefix = prefix.toLowerCase();
        }

        @Override
        public <T> @NotNull String serialize(final @NotNull T data) {
            throw getException();
        }

        @Override
        public <T> @NotNull T deserialize(final @NotNull String serialized,
                                          final @NotNull Class<T> dataType) {
            throw getException();
        }

        private @NotNull MapperException getException() {
            String moduleName = String.format("%s.%s:%s-%s",
                    ProjectInfo.GROUP,
                    ProjectInfo.PROJECT_NAME,
                    ProjectInfo.MODULE_NAME,
                    prefix
            );
            return new MapperException(
                    String.format("Could not find suitable %s for %s. ", Mapper.class.getCanonicalName(), prefix) +
                            String.format("Please check that the module %s is correctly installed.", moduleName)
            );
        }
    }

}
