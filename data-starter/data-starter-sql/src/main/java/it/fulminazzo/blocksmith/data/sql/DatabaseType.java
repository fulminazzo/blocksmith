package it.fulminazzo.blocksmith.data.sql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Most commonly used SQL databases.
 * For each one of them, an optimization method is present
 * in {@link RemoteDataSourceBuilder}.
 */
@RequiredArgsConstructor
public enum DatabaseType implements IDatabaseType {
    MYSQL(3306),
    MARIADB(3306),
    POSTGRESQL(5432),
    H2(8082),
    SQLITE(8191)
    ;

    @Getter
    private final int port;

    @Override
    public @NotNull String getJdbcName() {
        return name().toLowerCase();
    }

}
