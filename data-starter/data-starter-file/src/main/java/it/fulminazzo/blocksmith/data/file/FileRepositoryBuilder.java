package it.fulminazzo.blocksmith.data.file;

import org.jetbrains.annotations.NotNull;

/**
 * A builder for {@link FileRepository}.
 * <br>
 * Example usage:
 * <pre>{@code
 * FileRepository.builder(User.class)
 *         .dataDirectory(new File("./users"))
 *         .executor(ForkJoinPool.commonPool()) // proper executor should be used
 *         .dataLanguageFormat(ConfigurationFormat.JSON)
 *         .logger(LoggerFactory.getLogger(FileRepository.class))
 *         .idMapper(User::getId)
 *         .build();
 * }</pre>
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
 * }}</pre>
 *
 * @param <T> the type of the data
 */
public final class FileRepositoryBuilder<T> extends MappableFileRepositoryBuilder<T, FileRepositoryBuilder<T>> {

    /**
     * Instantiates a new File repository builder.
     *
     * @param dataType the data type
     */
    FileRepositoryBuilder(final @NotNull Class<T> dataType) {
        super(dataType);
    }

}
