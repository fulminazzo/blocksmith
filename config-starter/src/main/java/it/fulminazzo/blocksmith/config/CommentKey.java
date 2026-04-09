package it.fulminazzo.blocksmith.config;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Represents a key with comments.
 */
@Value
@AllArgsConstructor
public class CommentKey implements Comparable<CommentKey> {
    @NotNull String key;
    @EqualsAndHashCode.Exclude
    @NotNull List<String> comments;

    /**
     * Instantiates a new Comment key.
     *
     * @param key the key
     */
    public CommentKey(final @NotNull String key) {
        this(key, Collections.emptyList());
    }

    @Override
    public int compareTo(final @NotNull CommentKey commentKey) {
        return key.compareTo(commentKey.key);
    }

}
