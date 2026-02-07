package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * Identifies the type of data format language to utilize
 * for the {@link ConfigurationAdapter}.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ConfigurationFormat {

    ;

    @NotNull Function<Logger, ConfigurationAdapter> adapterSupplier;

    /**
     * Gets the adapter for the corresponding format.
     *
     * @param logger the logger
     * @return the adapter
     */
    @NotNull ConfigurationAdapter getAdapter(final @NotNull Logger logger) {
        return adapterSupplier.apply(logger);
    }

}
