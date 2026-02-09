package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A general {@link ASqlDataSourceBuilder} that provides internal parameters to be set.
 *
 * @param <B> the type parameter
 */
@SuppressWarnings("unchecked")
abstract class ParametersDataSourceBuilder<B extends ParametersDataSourceBuilder<B>> extends ASqlDataSourceBuilder<B> {
    private final @NotNull Map<String, List<String>> parameters = new HashMap<>();

    /**
     * Instantiates a new Parameters data source builder.
     *
     * @param config   the config
     * @param database the database
     */
    ParametersDataSourceBuilder(final @NotNull HikariConfig config,
                                final @Nullable String database) {
        super(config, database);
    }

    /**
     * Gets the parameters in the JDBC URL format.
     *
     * @return the parameters jdbc url
     */
    protected @NotNull String getParametersJdbcUrl() {
        return parameters.entrySet().stream()
                .map(e ->
                        String.format(";%s=%s", e.getKey(), String.join("\\;", e.getValue()))
                )
                .collect(Collectors.joining());
    }

    /**
     * Adds a new parameter to the final configuration of the database.
     *
     * @param name  the name of the parameter
     * @param value the value
     * @return this object (for method chaining)
     */
    public @NotNull B setParameters(final @NotNull String name,
                                    final @NotNull Object value) {
        return setParameters(name, value.toString().toUpperCase());
    }

    /**
     * Adds a new parameter to the final configuration of the database.
     *
     * @param name  the name of the parameter
     * @param value the value
     * @return this object (for method chaining)
     */
    public @NotNull B setParameters(final @NotNull String name,
                                    final @NotNull String value) {
        parameters.computeIfAbsent(name, v -> new ArrayList<>()).add(value);
        return (B) this;
    }

}
