package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.RepositorySettings;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings;
import it.fulminazzo.blocksmith.data.jooq.Tables;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings;
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings;
import it.fulminazzo.blocksmith.data.util.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConfigExample {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        User entity = new User(null, "Alex", "Drinkwater", "alex@fulminazzo.it", 23);

        ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML);
        DataSourceConfig config = adapter.load(
                new File("example/build/resources/main"),
                "database",
                DataSourceConfig.class
        );
        final RepositoryDataSource<RepositorySettings> dataSource = DataSourceFactories.build(config);

        //TODO
        @NotNull RepositorySettings combine = CachedRepositorySettings.combine(
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
        );
        Repository<User, Long> repository = dataSource.newRepository(
                User.class,
                combine
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
