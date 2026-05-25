package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import it.fulminazzo.blocksmith.data.CacheRepository;
import it.fulminazzo.blocksmith.data.CacheRepositoryDataSource;
import it.fulminazzo.blocksmith.data.entity.EntityMapper;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Function;

/**
 * Redis data source for handling connections and creating Redis repositories.
 * <br>
 * Manages Redis client connections and creates repositories for storing entities
 * in Redis using the Lettuce reactive client.
 * <br>
 * Examples:
 * <ul>
 *     <li>creation (local Redis):
 *         <pre>{@code
 *         RedisDataSource dataSource = RedisDataSource.builder()
 *                 // defaults to "127.0.0.1:6379"
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creation (remote Redis with authentication):
 *         <pre>{@code
 *         RedisDataSource dataSource = RedisDataSource.builder()
 *                 .uri(u -> u
 *                         .withHost("0.0.0.0")
 *                         .withPort(6379)
 *                         .withPassword("SuperSecurePassword")
 *                         .withSsl(true)
 *                 )
 *                 .build();
 *         }</pre>
 *     </li>
 *     <li>creating a standard repository:
 *         <pre>{@code
 *         RedisDataSource dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 EntityMapper.create(User.class),
 *                 new RedisRepositorySettings()
 *                         .withDatabaseName("users_db")
 *                         .withCollectionName("users")
 *         );
 *         }</pre>
 *     </li>
 *     <li>creating a custom repository:
 *         <pre>{@code
 *         RedisDataSource dataSource = ...;
 *         Repository<?, ?> repository = dataSource.newRepository(
 *                 engine -> new CustomRedisRepository<>(engine),
 *                 new RedisRepositorySettings()
 *                         .withDatabaseName("users_db")
 *                         .withCollectionName("users")
 *         );
 *         }</pre>
 *         where CustomRedisRepository extends {@link RedisRepository} and adds custom behavior
 *         such as per-value TTL-based expiration, pub/sub messaging or cache warming strategies.
 *     </li>
 * </ul>
 *
 * @see RedisRepositorySettings
 * @see RedisRepository
 * @see RedisQueryEngine
 */
public final class RedisDataSource implements CacheRepositoryDataSource<RedisRepositorySettings> {
    private final @NotNull RedisClient redisClient;
    private final @NotNull StatefulRedisConnection<String, String> connection;

    private final @NotNull Mapper mapper;

    /**
     * Instantiates a new Redis data source.
     *
     * @param redisClient the redis client
     * @param mapper      the mapper
     */
    RedisDataSource(final @NotNull RedisClient redisClient, final @NotNull Mapper mapper) {
        this.redisClient = redisClient;
        this.connection = redisClient.connect();
        this.mapper = mapper;
    }

    /**
     * Creates a new custom repository.
     *
     * @param <R>               the type of the repository
     * @param <T>               the type of the entities
     * @param <I>               the type of the id of the entities
     * @param repositoryBuilder the repository creation function
     * @param settings          the settings to build the repository with
     * @return the repository
     */
    @SuppressWarnings("unchecked")
    public <T, I, R extends RedisRepository<T, I>> @NotNull R newRepository(
            final @NotNull Function<RedisQueryEngine<T, I>, R> repositoryBuilder,
            final @NotNull RedisRepositorySettings settings
    ) {
        RedisQueryEngine<T, I> engine = new RedisQueryEngine<>(
                connection,
                (EntityMapper<T, I>) settings.getEntityMapper(),
                mapper,
                settings.getDatabaseName(),
                settings.getCollectionName()
        );
        final R repository = repositoryBuilder.apply(engine);
        Duration expiry = settings.getTtl();
        if (expiry != null) repository.ttl(expiry);
        return repository;
    }

    @Override
    public <T, I> @NotNull CacheRepository<T, I> newRepository(
            final @NotNull EntityMapper<T, I> entityMapper,
            final @NotNull RedisRepositorySettings settings
    ) {
        return newRepository(
                e -> new RedisRepository<>(e, entityMapper),
                settings.withEntityMapperIfNotSet(entityMapper)
        );
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
