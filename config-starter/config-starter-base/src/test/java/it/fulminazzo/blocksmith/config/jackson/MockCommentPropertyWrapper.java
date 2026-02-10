package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import it.fulminazzo.blocksmith.config.Comment;
import org.jetbrains.annotations.NotNull;

public class MockCommentPropertyWrapper extends CommentPropertyWriter {

    public MockCommentPropertyWrapper(final @NotNull BeanPropertyWriter base,
                                      final @NotNull Comment comment) {
        super(base, comment);
    }

    @Override
    protected void writeComment(final @NotNull JsonGenerator generator,
                                final @NotNull Comment comment) {

    }

}
