package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

/**
 * Redis datasource for handling connections and create Redis repositories.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation:
 *         <pre>{@code
 *         RedisDataSource dataSource = RedisDataSource.builder()
 *                 .uri(u -> u
 *                         .withHost("127.0.0.1") // defaults to "0.0.0.0"
 *                         .withPort(6379) // defaults to 6379
 *                         .withPassword("SuperSecurePassword")
 *                         .withSsl(true)
 *                 )
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating new repository:
 *         <pre>{@code
 *         RedisDataSource dataSource = ...;
 *         Class<?> dataType = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(dataType);
 *         }</pre>
 *         or, for more control:
 *         <pre>{@code
 *         Repository<?, ?> repository = dataSource.newRepository(EntityMapper.create(dataType, "idFieldName"));
 *         }</pre>
 *     </li>
 * </ul>
 */
public final class RedisDataSource implements RepositoryDataSource {
    private final @NotNull RedisClient redisClient;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new Redis datasource.
     *
     * @param redisClient the redis client
     * @param mapper      the mapper
     */
    RedisDataSource(final @NotNull RedisClient redisClient,
                    final @NotNull Mapper mapper) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.mapper = mapper;
    }

    /**
     * Creates a new repository.
     *
     * @param <T>        the type of the entities
     * @param <ID>       the type of the id of the entities
     * @param entityType the entity Java class
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull Class<T> entityType
    ) {
        return newRepository(EntityMapper.create(entityType));
    }

    /**
     * Creates a new repository.
     *
     * @param <T>          the type of the entities
     * @param <ID>         the type of the id of the entities
     * @param entityMapper the entities mapper
     * @return the repository
     */
    public <T, ID> @NotNull Repository<T, ID> newRepository(
            final @NotNull EntityMapper<T, ID> entityMapper
    ) {
        RedisQueryEngine<T, ID> engine = new RedisQueryEngine<>(
                connection,
                entityMapper,
                mapper
        );
        return new RedisRepository<>(engine, entityMapper);
    }

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }

    /**
     * Gets a new builder for this class.
     *
     * @return the builder
     */
    public static @NotNull RedisDataSourceBuilder builder() {
        return new RedisDataSourceBuilder();
    }

}
