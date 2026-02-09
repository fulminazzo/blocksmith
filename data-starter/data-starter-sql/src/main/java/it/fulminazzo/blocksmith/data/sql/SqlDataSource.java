package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.Closeable;

public final class SqlDataSource implements DataSource, Closeable {
    @Delegate
    private final @NotNull HikariDataSource delegate;
    private final @NotNull DSLContext context;

    SqlDataSource(final @NotNull HikariDataSource delegate,
                  final @NotNull SQLDialect dialect) {
        this.delegate = delegate;
        this.context = DSL.using(this, dialect);
    }

    public static @NotNull SqlDataSourceBuilder builder() {
        return new SqlDataSourceBuilder();
    }

}
