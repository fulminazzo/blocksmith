package it.fulminazzo.blocksmith.data;

import it.fulminazzo.blocksmith.data.cache.CachedDataSource;
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings;
import it.fulminazzo.blocksmith.data.cache.HybridCachedDataSource;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import it.fulminazzo.blocksmith.data.redis.RedisDataSource;
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings;
import it.fulminazzo.blocksmith.data.sql.DatabaseType;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings;
import it.fulminazzo.blocksmith.function.RunnableException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        double memory = time(() -> repository.findById(id).get());
        Thread.sleep(1_000);
        double redis = time(() -> repository.findById(id).get());
        Thread.sleep(5_000);
        double sql = time(() -> repository.findById(id).get());

        dataSource.close();

        System.out.println("Test results:\n" +
                String.format("Memory: %s ms\n", memory) +
                String.format("Redis: %s ms\n", redis) +
                String.format("SQL: %s ms", sql)
        );
    }

    private static double time(final @NotNull RunnableException<Exception> task) {
        try {
            long curr = System.nanoTime();
            task.run();
            long time = System.nanoTime() - curr;
            double millis = time / 1_000_000.0;
            return Math.round(millis * 100.0) / 100.0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static final class User {
        Long id;
        String name;
        String lastname;
        String email;
        Integer age;

    }

}
