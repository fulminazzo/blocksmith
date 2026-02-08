package it.fulminazzo.blocksmith.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockConfig {

    @Comment("Example comment")
    boolean commentsEnabled = true;

    @Comment({
            "This comment should be",
            "Multiline!"
    })
    String name = "blocksmith";

    @Comment("") // this comment should not appear
    String description = "This is the description for the configuration file.\n" +
            "Should be written in multiline format.";

    List<String> authors = Arrays.asList("Fulminazzo", "Camilla");

    Internal internal = new Internal();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Internal {

        @Comment("This comment should be indented\nAnd should be multiline too")
        double version = 1.0;

        Boolean verified = null;

    }

}
