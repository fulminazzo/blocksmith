package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import it.fulminazzo.blocksmith.config.Comment;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Mock {@link CommentPropertyWriter} for testing purposes.
 */
public class MockCommentPropertyWrapper extends CommentPropertyWriter {
    @Serial
    private static final long serialVersionUID = 5633454614512094400L;

    /**
     * Instantiates a new Mock comment property wrapper.
     *
     * @param base    the base
     * @param comment the comment
     */
    public MockCommentPropertyWrapper(final @NotNull BeanPropertyWriter base, final @NotNull Comment comment) {
        super(base, comment);
    }

    @Override
    protected void writeComment(final @NotNull JsonGenerator generator, final @NotNull Comment comment) {
        // comments not supported
    }

}
