package it.fulminazzo.blocksmith.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a general data holder for repository settings.
 * Implementation may vary according to the repository type.
 *
 * @see CacheRepositorySettings
 * @see Repository
 * @see RepositoryDataSource
 */
@EqualsAndHashCode
@ToString
public abstract class RepositorySettings {
    /*
     * This is a marker interface.
     * Submodules should use it to provide their own implementation.
     */
}
