package it.fulminazzo.config.jackson;

import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * A special type of {@link SettableBeanProperty} that will not throw
 * exceptions during deserialization and setting operations,
 * but will instead warn about the error and use the default value of the bean.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class LoggerSettableBeanProperty extends SettableBeanProperty.Delegating {
    @NotNull Logger logger;
    @NotNull AnnotatedField field;

    /**
     * Instantiates a new Logger settable bean property.
     *
     * @param delegate the delegate bean property that will handle the logic
     * @param logger   the logger to warn about any errors
     * @param field    the field that this property represents
     */
    public LoggerSettableBeanProperty(final @NotNull SettableBeanProperty delegate,
                                      @NotNull Logger logger,
                                      @NotNull AnnotatedField field) {
        super(delegate);
        this.logger = logger;
        this.field = field;
    }

    @Override
    protected SettableBeanProperty withDelegate(final @NotNull SettableBeanProperty delegate) {
        return new LoggerSettableBeanProperty(delegate, logger, field);
    }

    /**
     * Represents an exception during deserialization.
     */
    static final class DeserializationException extends RuntimeException {

        /**
         * Instantiates a new Deserialization exception.
         *
         * @param message   the message
         * @param arguments the arguments to format in the message
         */
        public DeserializationException(final @NotNull String message,
                                        final Object @NotNull ... arguments) {
            super(String.format(message, arguments));
        }

    }

}
