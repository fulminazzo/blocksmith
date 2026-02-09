package it.fulminazzo.blocksmith.data.sql;

import org.jetbrains.annotations.NotNull;

public enum DatabaseType implements IDatabaseType {
    MYSQL,
    MARIADB,
    POSTGRESQL
    ;

    @Override
    public @NotNull String getJdbcName() {
        return name().toLowerCase();
    }

}
