package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.RepositorySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.Objects;

public final class SqlRepositorySettings extends RepositorySettings {
    private @Nullable Table<?> table;
    private @Nullable TableField<?, ?> idColumn;

    public @NotNull Table<?> getTable() {
        return Objects.requireNonNull(table, "table has not been specified yet");
    }

    public @NotNull TableField<?, ?> getIdColumn() {
        return Objects.requireNonNull(idColumn, "id column has not been specified yet");
    }

    public @NotNull SqlRepositorySettings withTable(final @NotNull Table<?> table) {
        this.table = table;
        return this;
    }

    public @NotNull SqlRepositorySettings withIdColumn(final @NotNull TableField<?, ?> idColumn) {
        this.idColumn = idColumn;
        return this;
    }

}
