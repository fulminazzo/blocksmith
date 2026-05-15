//file:noinspection GrMethodMayBeStatic
package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import org.h2.tools.Server
import org.jooq.SQLDialect

import javax.sql.DataSource

class H2RemoteIntegrationTestHelper extends SqlIntegrationTestHelper {
    private static final String SERVER_HOST = 'localhost'
    private static final int SERVER_PORT = DatabaseType.H2.port
    private static final String H2_PATH = "jdbc:h2:tcp://$SERVER_HOST:${SERVER_PORT}/./build/resources/integrationTest/h2_remote"

    private Server server

    String getServerHost() {
        return SERVER_HOST
    }

    int getServerPort() {
        return SERVER_PORT
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.H2
    }

    @Override
    void close() throws IOException {
        super.close()
        server?.stop()
    }

    @Override
    protected DataSource newDataSource() {
        server = Server.createTcpServer(
                '-tcpPort', String.valueOf(SERVER_PORT),
                '-tcpAllowOthers',
                '-ifNotExists'
        ).start()

        def config = new HikariConfig()
        config.jdbcUrl = H2_PATH
        config.username = 'root'
        config.password = 'test'

        return new HikariDataSource(config)
    }

}
