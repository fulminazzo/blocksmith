package it.fulminazzo.blocksmith.data.sql;

import org.jetbrains.annotations.NotNull;

/**
 * Most commonly used SQL databases.
 * For each one of them, an optimization method is present
 * in {@link RemoteDataSourceBuilder}.
 */
public enum DatabaseType implements IDatabaseType {
    MYSQL,
    MARIADB,
    POSTGRES
    ;

    @Override
    public @NotNull String getJdbcName() {
        return name().toLowerCase();
    }

}
