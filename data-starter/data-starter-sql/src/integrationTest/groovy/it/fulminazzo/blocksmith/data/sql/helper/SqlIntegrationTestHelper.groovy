package it.fulminazzo.blocksmith.data.sql.helper

import org.jooq.*
import org.jooq.impl.SQLDataType

import javax.sql.DataSource

import static org.jooq.impl.DSL.constraint
import static org.jooq.impl.DSL.using

/**
 * A helper for interacting with a certain SQL database.
 */
abstract class SqlIntegrationTestHelper implements Closeable {
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    public final DSLContext context
    private final DataSource dataSource

    SqlIntegrationTestHelper() {
        dataSource = newDataSource()

        context = using(dataSource, dialect)
        context.createTableIfNotExists(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN))
                .execute()
    }

    Table<? extends Record> getTable() {
        return context.meta().getTables(TABLE_NAME).last
    }

    TableField<? extends Record, Long> getColumn() {
        return table.field(ID_COLUMN) as TableField<? extends Record, Long>
    }

    /**
     * Initializes a new {@link DataSource} connection to the database.
     *
     * @return the data source
     */
    protected abstract DataSource newDataSource()

    /**
     * Gets the dialect of the database.
     *
     * @return the dialect
     */
    protected abstract SQLDialect getDialect()

    @Override
    void close() throws IOException {
        dataSource?.close()
    }

}