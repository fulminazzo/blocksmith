package it.fulminazzo.blocksmith.data.file.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.file.FileDataSource;
import lombok.Value;

import java.util.concurrent.Executors;

/**
 * {@link DataSourceConfig} for {@link FileDataSource}.
 *
 * @see DataSourceConfig
 * @see FileDataSource
 */
@Value
public class FileDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                FileDataSourceConfig.class,
                c -> FileDataSource.create(Executors.newCachedThreadPool())
        );
    }

}
