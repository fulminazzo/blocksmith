package it.fulminazzo.blocksmith.data.memory.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.concurrent.Executors;

/**
 * {@link DataSourceConfig} for {@link MemoryDataSource}.
 *
 * @see DataSourceConfig
 * @see MemoryDataSource
 */
@Value
@Accessors(chain = true)
public class MemoryDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                MemoryDataSourceConfig.class,
                config -> MemoryDataSource.create(Executors.newCachedThreadPool())
        );
    }

}
