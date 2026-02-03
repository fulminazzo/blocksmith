package it.fulminazzo.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * A collection of utilities to work with jackson.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class JacksonUtils {

    /**
     * Given the parser, returns the path of the current context in a <b>dot notation</b>.
     *
     * @param parser the parser
     * @return the current path
     */
    public static @NotNull String getCurrentPath(final @NotNull JsonParser parser) {
        LinkedList<String> path = new LinkedList<>();

        JsonStreamContext context = parser.getParsingContext();
        while (context != null) {
            if (context.inArray()) path.addFirst(String.format("[%s]", context.getCurrentIndex()));
            else {
                String currentName = context.getCurrentName();
                if (currentName != null) path.addFirst("." + currentName);
            }
            context = context.getParent();
        }

        String finalPath = String.join("", path);
        if (!finalPath.isEmpty()) finalPath = finalPath.substring(1);
        return finalPath;
    }

}
