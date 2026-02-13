package it.fulminazzo.blocksmith.data;

import java.io.Closeable;

/**
 * Common interface for all repositories datasources.
 * <br>
 * Identifies all objects that can create repositories for entities.
 * This is a marker interface - each implementation varies in its methods
 * because each backend has different requirements.
 */
public interface RepositoryDataSource extends Closeable {

}
