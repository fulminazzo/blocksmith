package it.fulminazzo.blocksmith.data.mapper;

import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {
    public static final @NotNull Mapper SERIALIZABLE = getMapper("Serializable");
    public static final @NotNull Mapper JSON = getMapper("Json");

    @SuppressWarnings("unchecked")
    static @NotNull Mapper getMapper(final @NotNull String prefix) {
        String className = Mapper.class.getPackageName() + "." + prefix + Mapper.class.getSimpleName();
        try {
            Class<Mapper> type = (Class<Mapper>) Class.forName(className);
            return Reflect.on(type).init().get();
        } catch (ClassNotFoundException e) {
            return new MissingMapper(prefix);
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
