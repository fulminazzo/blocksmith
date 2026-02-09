package it.fulminazzo.blocksmith.data.sql;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general Database type.
 */
public interface IDatabaseType {

    /**
     * Gets the name of this database type for the JDBC URL.
     *
     * @return the jdbc name
     */
    @NotNull String getJdbcName();

}
