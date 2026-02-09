package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import it.fulminazzo.blocksmith.data.Repository;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;

public final class SqlDataSource implements DataSource, Closeable {
    @Delegate
    private final @NotNull HikariDataSource delegate;
    private final @NotNull DSLContext context;

    SqlDataSource(final @NotNull HikariDataSource delegate,
                  final @NotNull SQLDialect dialect) {
        this.delegate = delegate;
        this.context = DSL.using(this, dialect);
    }

    public <T, ID> @NotNull Repository<T, ID> newRepository(final @NotNull Table<?> table,
                                                            final @NotNull Class<T> dataType,
                                                            final @NotNull Class<ID> idType,
                                                            final @NotNull Executor executor) {
        @Nullable UniqueKey<?> primaryKey = table.getPrimaryKey();
        if (primaryKey == null)
            throw new IllegalArgumentException("Could not find a primary key from table: " + table);
        List<? extends TableField<?, ?>> fields = primaryKey.getFields();
        if (fields.size() != 1)
            throw new IllegalArgumentException("Only primary keys with one column as primary key are supported");
        return new SqlRepository<>(
                context,
                table,
                fields.get(0).cast(idType),
                dataType,
                executor
        );
    }

    public static @NotNull SqlDataSourceBuilder builder() {
        return new SqlDataSourceBuilder();
    }

}
