package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.config.Comment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.fulminazzo.blocksmith.validation.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * The blocksmith configuration
 */
public final class BlocksmithEnhancedConfig {

    @Comment({
            "Server settings of the application.",
            "Set these accordingly!"
    })
    @NotNull
    @NonNull
    private Server server = new Server();

    @Comment("Name of the application")
    @NotNull
    @NonNull
    @Alphabetical
    private String name = "blocksmith";

    @NotNull
    @NonNull
    private List<String> authors = new ArrayList<>(Arrays.asList("Fulminazzo"));

    @Nullable
    private Object lastUpdate = null;

    @NotNull
    public Server getServer() {
        return server;
    }

    public void setServer(@NotNull final Server server) {
        this.server = server;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull @NonNull @Alphabetical final String name) {
        this.name = name;
    }

    @NotNull
    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(@NotNull @NonNull final List<String> authors) {
        this.authors = authors;
    }

    @Nullable
    public Object getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@Nullable final Object lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public static final class Server {

        @NotNull
        @NonNull
        @Hostname
        private String host = "localhost";

        @NotNull
        @NonNull
        @Port
        private Integer port = 8080;

        @NotNull
        public String getHost() {
            return host;
        }

        public void setHost(@NotNull @NonNull @Hostname final String host) {
            this.host = host;
        }

        @NotNull
        public Integer getPort() {
            return port;
        }

        public void setPort(@NotNull @NonNull @Port final Integer port) {
            this.port = port;
        }
    }
}
