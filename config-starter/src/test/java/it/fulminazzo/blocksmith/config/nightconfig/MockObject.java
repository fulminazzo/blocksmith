package it.fulminazzo.blocksmith.config.nightconfig;

import it.fulminazzo.blocksmith.config.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockObject {

    String simple = "Hello, world!";

    @Comment("This is the first comment")
    double version = 1.0;

    @Comment("This comment is multiline\n" +
            "Hope it will work!")
    int players = 2;

    Boolean allowed = null;

    MockObject current = null;

    Map<String, String> authors = new HashMap<>(){{
        put("Alex", "Fulminazzo");
        put("Camilla", "Drinkwater");
    }};

    @Comment("Special mentions")
    List<String> mentions = List.of("Frank");

    @Comment("Internal data, should not be used")
    Internal internal = new Internal();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Internal {

        @Comment("This comment is internal")
        double javaVersion = 11;

        GradleVersion gradleVersion = new GradleVersion();

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradleVersion {

        @Comment("Gradle version")
        double gradle = 8.14;

        @Comment("Groovy version")
        double groovy = 4.0;

    }

}
