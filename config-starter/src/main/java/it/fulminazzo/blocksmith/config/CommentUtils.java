package it.fulminazzo.blocksmith.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A collection of utilities for {@link Comment}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentUtils {

    /**
     * Checks if the given comment is empty.
     *
     * @param comment the comment
     * @return <code>true</code> if it is
     */
    public static boolean isEmpty(final @NotNull Comment comment) {
        return getText(comment).stream().allMatch(l -> l.trim().isEmpty());
    }

    /**
     * Gets the text of the comment.
     * Each entry of the collection will be a line of the comment.
     *
     * @param comment the comment
     * @return the comment text
     */
    public static @NotNull Collection<String> getText(final @NotNull Comment comment) {
        List<String> text = new ArrayList<>();
        for (String t : comment.value()) {
            String[] raw = t.replaceAll("\\n", "\n").split("\n");
            text.addAll(Arrays.asList(raw));
        }
        return text;
    }

}
