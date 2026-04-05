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

    private Object lastUpdate = null;

    @Comment("Name of the application")
    private String name = "blocksmith";

    private List<String> authors = new ArrayList<>(Arrays.asList("Fulminazzo"));

    private String[] test = new String[]{"hello", "world"};

    public Server getServer() {
        return server;
    }

    public void setServer(final Server server) {
        this.server = server;
    }

    public Object getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Object lastUpdate) {
        this.lastUpdate = lastUpdate;
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

    public String[] getTest() {
        return test;
    }

    public void setTest(final String[] test) {
        this.test = test;
    }

    public static final class Server {

        private Integer port = 8080;

        private String host = "localhost";

        public Integer getPort() {
            return port;
        }

        public void setPort(final Integer port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }
    }
}
