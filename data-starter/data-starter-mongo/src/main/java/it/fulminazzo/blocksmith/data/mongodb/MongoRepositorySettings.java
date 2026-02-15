package it.fulminazzo.blocksmith.data.mongodb;

import it.fulminazzo.blocksmith.data.RepositorySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class MongoRepositorySettings extends RepositorySettings {
    private @Nullable String databaseName;
    private @Nullable String collectionName;
    private @Nullable Class<?> entityType;

    public @NotNull String getDatabaseName() {
        return Objects.requireNonNull(databaseName, "database name has not been specified yet");
    }

    public @NotNull String getCollectionName() {
        return Objects.requireNonNull(collectionName, "collection name has not been specified yet");
    }

    public @NotNull Class<?> getEntityType() {
        return Objects.requireNonNull(entityType, "entity type has not been specified yet");
    }

    public @NotNull MongoRepositorySettings withDatabaseName(final @NotNull String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public @NotNull MongoRepositorySettings withCollectionName(final @NotNull String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public @NotNull MongoRepositorySettings withEntityType(final @NotNull Class<?> entityType) {
        this.entityType = entityType;
        return this;
    }

}
