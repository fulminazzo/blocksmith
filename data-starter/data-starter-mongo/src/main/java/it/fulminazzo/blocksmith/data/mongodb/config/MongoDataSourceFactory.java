package it.fulminazzo.blocksmith.data.mongodb.config;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import it.fulminazzo.blocksmith.data.RepositoryDataSource;
import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactory;
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSource;
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSourceBuilder;
import org.jetbrains.annotations.NotNull;

final class MongoDataSourceFactory implements DataSourceFactory {

    @Override
    public @NotNull RepositoryDataSource<?> build(final @NotNull DataSourceConfig config) {
        MongoDataSourceConfig dsConfig = (MongoDataSourceConfig) config;
        Integer port = dsConfig.getPort();
        MongoDataSourceBuilder builder = MongoDataSource.builder()
                .host(dsConfig.getHost(), port == null ? ServerAddress.defaultPort() : port);
        
        String srvHost = dsConfig.getSrvHost();
        if (srvHost != null) builder.srvHost(srvHost);

        Integer srvMaxHosts = dsConfig.getSrvMaxHosts();
        if (srvMaxHosts != null) builder.srvMaxHosts(srvMaxHosts);

        String srvServiceName = dsConfig.getSrvServiceName();
        if (srvServiceName != null) builder.srvServiceName(srvServiceName);

        String replicaSetName = dsConfig.getSrvServiceName();
        if (replicaSetName != null) builder.replicaSetName(replicaSetName);

        String applicationName = dsConfig.getSrvServiceName();
        if (applicationName != null) builder.applicationName(applicationName);

        MongoDataSourceConfig.MongoCredentialConfig credentials = dsConfig.getCredentials();
        if (credentials != null) {
            String authSource = credentials.getAuthSource();
            if (authSource == null) authSource = "admin";
            MongoCredential credential = MongoCredential.createCredential(
                    credentials.getUsername(),
                    authSource,
                    credentials.getPassword().toCharArray()
            );
            String mechanism = credentials.getMechanism();
            if (mechanism != null)
                credential = credential.withMechanism(AuthenticationMechanism.valueOf(mechanism));
            builder.credential(credential);
        }
        
        return builder.build();
    }

}
