package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.config.Comment;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import it.fulminazzo.blocksmith.validation.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * The blocksmith configuration
 */
@Value
public class BlocksmithEnhancedConfig {

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

    public static final class Server {

        @NotNull
        @NonNull
        @Hostname
        @Getter
        private String host = "localhost";

        @NotNull
        @NonNull
        @Port
        @Setter
        private Integer port = 8080;

        public void setHost(@NotNull @NonNull @Hostname final String host) {
            this.host = host;
        }

        @NotNull
        public Integer getPort() {
            return port;
        }
    }
}
