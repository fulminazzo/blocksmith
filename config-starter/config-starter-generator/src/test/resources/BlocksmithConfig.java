package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.config.Comment;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public final class BlocksmithConfig {

    @Comment({
            "Server settings of the application.",
            "Set these accordingly!"
    })
    private Server server = new Server();

    @Comment("Name of the application")
    private String name = "blocksmith";

    private List<String> authors = new ArrayList<>(Arrays.asList("Fulminazzo"));

    private Object lastUpdate = null;

    public Server getServer() {
        return server;
    }

    public void setServer(final Server server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(final List<String> authors) {
        this.authors = authors;
    }

    public Object getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Object lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public static final class Server {

        private String host = "localhost";

        private Integer port = 8080;

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(final Integer port) {
            this.port = port;
        }
    }
}
