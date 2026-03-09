package it.fulminazzo.blocksmith.data.file.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.file.FileDataSource;
import lombok.Value;

import java.util.concurrent.Executors;

@Value
public final class FileDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                FileDataSourceConfig.class,
                c -> FileDataSource.create(Executors.newCachedThreadPool())
        );
    }

}
