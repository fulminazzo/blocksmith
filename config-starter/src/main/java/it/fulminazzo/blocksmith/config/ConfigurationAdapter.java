package it.fulminazzo.blocksmith.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * A specialized adapter to load and store <b>Java beans</b>
 * representing configuration files in different language data formats.
 * <br>
 * Supports interchangeability of data format languages.
 */
public interface ConfigurationAdapter extends BaseConfigurationAdapter {

    /**
     * Updates the format for this configuration adapter.
     *
     * @param format the format
     * @return this configuration adapter
     */
    @NotNull ConfigurationAdapter setFormat(final @NotNull ConfigurationFormat format);

    /**
     * Instantiates a new Configuration adapter.
     *
     * @param logger the logger
     * @param format the format
     * @return the configuration adapter
     */
    static @NotNull ConfigurationAdapter newAdapter(final @NotNull Logger logger,
                                                    final @NotNull ConfigurationFormat format) {
        return new DelegateConfigurationAdapter(logger).setFormat(format);
    }

}
