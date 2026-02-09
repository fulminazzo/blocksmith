package it.fulminazzo.blocksmith.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.Closeable;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SqlDataSource implements DataSource, Closeable {
    @Delegate
    private final @NotNull HikariDataSource delegate;

    public static @NotNull SqlDataSourceBuilder builder() {
        return new SqlDataSourceBuilder();
    }

}
