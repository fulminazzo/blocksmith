package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.RepositorySettings;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.Objects;

/**
 * Repository settings for SQL databases.
 *
 * @see SqlRepository
 * @see SqlDataSource
 */
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlRepositorySettings extends RepositorySettings {
    private @Nullable Table<?> table;
    private @Nullable TableField<?, ?> idColumn;

    /**
     * Sets the table containing the entities.
     *
     * @param table the table
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositorySettings withTable(final @NotNull Table<?> table) {
        this.table = table;
        return this;
    }

    /**
     * Sets the column representing the ID of the entities.
     *
     * @param idColumn the id column
     * @return this object (for method chaining)
     */
    public @NotNull SqlRepositorySettings withIdColumn(final @NotNull TableField<?, ?> idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    /**
     * Gets the table containing the entities.
     *
     * @return the table
     */
    public @NotNull Table<?> getTable() {
        return Objects.requireNonNull(table, "table has not been specified yet");
    }

    /**
     * Gets the column representing the ID of the entities.
     *
     * @return the column
     */
    public @NotNull TableField<?, ?> getIdColumn() {
        return Objects.requireNonNull(idColumn, "id column has not been specified yet");
    }

}
