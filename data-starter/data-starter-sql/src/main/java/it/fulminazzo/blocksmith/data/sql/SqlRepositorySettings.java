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
@With
public final class SqlRepositorySettings extends RepositorySettings {
    private @Nullable Table<?> table;
    private @Nullable TableField<?, ?> idColumn;

    public @NotNull Table<?> getTable() {
        return Objects.requireNonNull(table, "table has not been specified yet");
    }

    public @NotNull TableField<?, ?> getIdColumn() {
        return Objects.requireNonNull(idColumn, "id column has not been specified yet");
    }

}
