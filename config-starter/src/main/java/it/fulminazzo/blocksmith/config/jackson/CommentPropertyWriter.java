package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import it.fulminazzo.blocksmith.config.Comment;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A special {@link BeanPropertyWriter} that supports
 * writing of comments for a property.
 */
public abstract class CommentPropertyWriter extends BeanPropertyWriter {
    private final @NotNull Comment comment;

    /**
     * Instantiates a new Comment property writer.
     *
     * @param base    the base
     * @param comment the comment
     */
    public CommentPropertyWriter(final @NotNull BeanPropertyWriter base,
                                 final @NotNull Comment comment) {
        super(base);
        this.comment = comment;
    }

    @Override
    public void serializeAsField(final Object bean,
                                 final JsonGenerator generator,
                                 final SerializerProvider provider) throws Exception {
        writeComment(generator, comment);
        super.serializeAsField(bean, generator, provider);
    }

    /**
     * Writes the given comment using the generator.
     *
     * @param generator the generator
     * @param comment   the comment
     * @throws IOException in case of any errors
     */
    protected abstract void writeComment(final @NotNull JsonGenerator generator,
                                         final @NotNull Comment comment) throws IOException;

}
