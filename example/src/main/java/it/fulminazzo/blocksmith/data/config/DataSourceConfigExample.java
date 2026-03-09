package it.fulminazzo.blocksmith.data.config;

import it.fulminazzo.blocksmith.config.ConfigurationAdapter;
import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.*;
import it.fulminazzo.blocksmith.data.file.FileRepositorySettings;
import it.fulminazzo.blocksmith.data.jooq.Tables;
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings;
import it.fulminazzo.blocksmith.data.mongodb.MongoRepositorySettings;
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings;
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings;
import it.fulminazzo.blocksmith.data.util.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        File directory = new File("example/build/resources/main");
        DataSourceConfig config = adapter.load(
                directory,
                "database",
                DataSourceConfig.class
        );
        final RepositoryDataSource<RepositorySettings> dataSource = DataSourceFactories.build(config);

        AllRepositorySettings settings = AllRepositorySettings.builder()
                .memory(new MemoryRepositorySettings().withTtl(Duration.ofSeconds(1L)))
                .file(new FileRepositorySettings()
                        .withDataDirectory(directory)
                        .withFormat(ConfigurationFormat.JSON)
                        .withLogger(log)
                )
                .sql(new SqlRepositorySettings()
                        .withTable(Tables.USERS)
                        .withIdColumn(Tables.USERS.ID))
                .redis(new RedisRepositorySettings()
                        .withDatabaseName("test")
                        .withCollectionName("users")
                        .withTtl(Duration.ofSeconds(5L)))
                .mongo(new MongoRepositorySettings()
                        .withDatabaseName("test")
                        .withCollectionName("users"))
                .build();

        Repository<User, Long> repository = dataSource.newRepository(
                User.class,
                settings.getRepositorySettings(dataSource)
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
