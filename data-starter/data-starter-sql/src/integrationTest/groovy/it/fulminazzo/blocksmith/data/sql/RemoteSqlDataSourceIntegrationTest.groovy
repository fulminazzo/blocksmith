package it.fulminazzo.blocksmith.data.sql

abstract class RemoteSqlDataSourceIntegrationTest extends SqlDataSourceIntegrationTest {

    protected abstract IDatabaseType getDatabaseType()

    @Override
    protected <B extends ASqlDataSourceBuilder<B>> B newDataSourceBuilderImpl() {
        return SqlDataSource.builder()
                .databaseType(databaseType)
                .port(null)
                .host(testHelper.serverHost)
                .port(testHelper.serverPort)
    }

}
