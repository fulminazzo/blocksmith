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

    final DSLContext context
    private final DataSource dataSource

    protected SqlIntegrationTestHelper() {
        dataSource = newDataSource()

        context = initializeContextAndTable(dataSource, dialect)
    }

    /**
     * Gets the dialect of the database.
     *
     * @return the dialect
     */
    abstract SQLDialect getDialect()

    /**
     * Initializes a new {@link DataSource} connection to the database.
     *
     * @return the data source
     */
    protected abstract DataSource newDataSource()

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    Table<? extends Record> getTable() {
        return context.meta().getTables(TABLE_NAME).last
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    TableField<? extends Record, Long> getColumn() {
        return table.field(ID_COLUMN) as TableField<? extends Record, Long>
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    @Override
    void close() throws IOException {
        dataSource?.close()
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static DSLContext initializeContextAndTable(final DataSource dataSource, final SQLDialect dialect) {
        def context = using(dataSource, dialect)
        context.createTableIfNotExists(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN))
                .execute()
        return context
    }

}
