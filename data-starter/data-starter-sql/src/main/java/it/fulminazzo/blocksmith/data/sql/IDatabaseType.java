package it.fulminazzo.blocksmith.data.sql;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a general database type.
 * <br>
 * Before implementing this class, check {@link DatabaseType}
 * to see if an implementation is already available.
 * Also, do not USE it for <b>H2</b>, as a predefined method
 * is present for that type.
 */
public interface IDatabaseType {

    /**
     * Gets the identifier of this type in a JDBC url.
     * <br>
     * The jdbc url will be built with the following format:
     * {@code jdbc:<name>://<host>:<port>/<database_name>}
     *
     * @return the jdbc name
     */
    @NotNull String getJdbcName();

    /**
     * Gets the default port for this database type.
     *
     * @return the port
     */
    int getPort();

}
