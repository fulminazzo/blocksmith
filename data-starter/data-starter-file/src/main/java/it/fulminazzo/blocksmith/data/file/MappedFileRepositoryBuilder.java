package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A builder for {@link FileRepository}.
 * <br>
 * Example usage:
 * <pre>{@code
 * FileRepository.builder(User.class)
 *         .idMapper(User::getId)
 *         .dataDirectory(new File("./users"))
 *         .executor(ForkJoinPool.commonPool()) // proper executor should be used
 *         .dataLanguageFormat(ConfigurationFormat.JSON)
 *         .logger(LoggerFactory.getLogger(FileRepository.class))
 *         .build();
 * }*</pre>
 * <br>
 * Given the Java POJO
 * <pre>{@code
 * public class User {
 *
 *     private UUID id;
 *
 *     private String username;
 *
 *     public User() {
 *
 *     }
 *
 *     public User(UUID id, String username) {
 *         this.id = id;
 *         this.username = username;
 *     }
 *
 *     public UUID getId() {
 *         return id;
 *     }
 *
 *     public void setId(UUID id) {
 *         this.id = id;
 *     }
 *
 *     public String getUsername() {
 *         return username;
 *     }
 *
 *     public void setUsername(String username) {
 *         this.username = username;
 *     }
 *
 * }}*</pre>
 *
 * @param <T>  the type of the data
 * @param <ID> the type of the id
 */
public final class MappedFileRepositoryBuilder<T, ID> extends MappableFileRepositoryBuilder<T, MappedFileRepositoryBuilder<T, ID>> {
    private final @NotNull Function<T, ID> idMapper;

    /**
     * Instantiates a new Mapped file repository builder.
     *
     * @param dataDirectory the data directory
     * @param executor      the executor
     * @param format        the format
     * @param logger        the logger
     * @param dataType      the data type
     * @param idMapper      the id mapper
     */
    MappedFileRepositoryBuilder(final @Nullable File dataDirectory,
                                final @Nullable Executor executor,
                                final @Nullable ConfigurationFormat format,
                                final @Nullable Logger logger,
                                final @NotNull Class<T> dataType,
                                final @NotNull Function<T, ID> idMapper) {
        super(dataDirectory, executor, format, logger, dataType);
        this.idMapper = idMapper;
    }

    /**
     * Creates a new File repository.
     *
     * @return the file repository
     */
    public @NotNull FileRepository<T, ID> build() {
        return new FileRepository<>(
                Objects.requireNonNull(dataDirectory, "'dataDirectory' requires to be specified"),
                dataType,
                idMapper,
                Objects.requireNonNull(executor, "'executor' requires to be specified"),
                Objects.requireNonNull(logger, "'logger' requires to be specified"),
                Objects.requireNonNull(format, "'format' requires to be specified")
        );
    }

}
