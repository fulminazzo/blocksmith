package it.fulminazzo.blocksmith.data.sql

abstract class RemoteSqlDataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    protected abstract DatabaseType getDatabaseType()

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder()
                .databaseType(databaseType)
                .host(testHelper.serverHost)
                .port(testHelper.serverPort)
                .mysql()
    }

}
