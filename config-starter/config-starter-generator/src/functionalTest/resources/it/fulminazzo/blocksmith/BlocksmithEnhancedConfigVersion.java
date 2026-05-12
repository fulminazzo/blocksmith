package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.config.Comment;
import it.fulminazzo.blocksmith.config.ConfigVersion;
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
public class BlocksmithEnhancedConfigVersion {

    private static final ConfigVersion version = ConfigVersion.of(2.0)
            .migrate(1.0, m -> m.rename("host", "server.host")
                    .add("server.port", 8080))
            .migrate(1.5, m -> m.add("lastUpdate", null));

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
