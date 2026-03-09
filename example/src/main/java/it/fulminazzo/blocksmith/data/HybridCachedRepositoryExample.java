package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings;
import it.fulminazzo.blocksmith.data.cache.HybridCachedDataSource;
import it.fulminazzo.blocksmith.data.jooq.Tables;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import it.fulminazzo.blocksmith.data.redis.RedisDataSource;
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings;
import it.fulminazzo.blocksmith.data.sql.DatabaseType;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings;
import it.fulminazzo.blocksmith.data.util.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HybridCachedRepositoryExample {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        User entity = new User(null, "Alex", "Drinkwater", "alex@fulminazzo.it", 23);

        final HybridCachedDataSource<RedisRepositorySettings, SqlRepositorySettings> dataSource = CachedDataSource.hybrid(
                MemoryDataSource.create(executor),
                RedisDataSource.builder().build(),
                SqlDataSource.builder()
                        .executor(executor)
                        .database("test")
                        .username("root")
                        .password("SuperSecurePassword")
                        .databaseType(DatabaseType.MARIADB)
                        .build()
                        .executeScriptFromResource("/schema.sql")
        );

        Repository<User, Long> repository = dataSource.newRepository(
                User.class,
                CachedRepositorySettings.combine(
                        new MemoryRepositorySettings().withTtl(Duration.ofSeconds(1L)),
                        CachedRepositorySettings.combine(
                                new RedisRepositorySettings()
                                        .withDatabaseName("test")
                                        .withCollectionName("users")
                                        .withTtl(Duration.ofSeconds(5L)),
                                new SqlRepositorySettings()
                                        .withTable(Tables.USERS)
                                        .withIdColumn(Tables.USERS.ID)
                        )
                )
        );

        long id = repository.save(entity).get().getId();

        double memory = TimeUtils.time(() -> repository.findById(id).get());
        Thread.sleep(1_000);
        double redis = TimeUtils.time(() -> repository.findById(id).get());
        Thread.sleep(5_000);
        double sql = TimeUtils.time(() -> repository.findById(id).get());

        dataSource.close();

        System.out.println("Test results:\n" +
                String.format("Memory: %s ms\n", memory) +
                String.format("Redis: %s ms\n", redis) +
                String.format("SQL: %s ms", sql)
        );
    }

}
