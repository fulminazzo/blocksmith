package it.fulminazzo.blocksmith.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockConfig {

    @Comment("Example comment")
    boolean commentsEnabled = true;

    @Comment("This comment should be\n" +
            "Multiline!")
    String name = "blocksmith";

    Internal internal = new Internal();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Internal {

        @Comment("This comment should be indented")
        double version = 1.0;

    }

}
