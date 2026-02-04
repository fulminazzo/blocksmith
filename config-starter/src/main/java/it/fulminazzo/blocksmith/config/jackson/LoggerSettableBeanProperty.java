package it.fulminazzo.blocksmith.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import jakarta.validation.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * A special type of {@link SettableBeanProperty} that will not throw
 * exceptions during deserialization and setting operations,
 * but will instead warn about the error and use the default value of the bean.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class LoggerSettableBeanProperty extends SettableBeanProperty.Delegating {
    @NotNull Logger logger;
    @NotNull AnnotatedField field;
    @NotNull Validator validator;

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
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Override
    protected SettableBeanProperty withDelegate(final @NotNull SettableBeanProperty delegate) {
        return new LoggerSettableBeanProperty(delegate, logger, field);
    }

    @Override
    public void set(final @NotNull Object instance, final Object value) throws IOException {
        validateValue(instance.getClass(), value);
        super.set(instance, value);
    }

    private <T> void validateValue(final @NotNull Class<T> beanType, final @Nullable Object value) {
        Set<ConstraintViolation<T>> violations = validator.validateValue(beanType, field.getName(), value);
        Optional<ConstraintViolation<T>> first = violations.stream().findFirst();
        if (first.isPresent()) throw new ViolationException(first.get());
    }

    @Override
    public void deserializeAndSet(final @NotNull JsonParser parser,
                                  final @NotNull DeserializationContext context,
                                  final @NotNull Object instance) {
        deserializeSetAndReturn(parser, context, instance);
    }

    @Override
    public Object deserializeSetAndReturn(final @NotNull JsonParser parser,
                                          final @NotNull DeserializationContext context,
                                          final @NotNull Object instance) {
        try {
            Object value = deserialize(parser, context);
            set(instance, value);
            return value;
        } catch (DeserializationException e) {
            return handleDeserializationException(instance, e);
        } catch (Exception e) {
            return handleGeneralException(parser, instance, e);
        }
    }

    private Object handleDeserializationException(final @NotNull Object instance,
                                                  final @NotNull DeserializationException exception) {
        logger.warn(exception.getMessage()
                .replace("<name>", field.getName())
                .replace("<type>", field.getRawType().getCanonicalName())
        );
        return getAndLogDefaultValueUsage(instance);
    }

    private Object handleGeneralException(final @NotNull JsonParser parser,
                                          final @NotNull Object instance,
                                          final @NotNull Exception exception) {
        String path = JacksonUtils.getCurrentPath(parser);
        String message = exception.getMessage();
        if (message == null) message = "unknown error";
        message = message.split("\n")[0];
        logger.warn("Invalid value for property '{}': {} (path: {})", field.getName(), message, path);
        if (!(exception instanceof ViolationException))
            logger.debug("Invalid value for property '{}': {} (path: {})", field.getName(), message, path, exception);
        return getAndLogDefaultValueUsage(instance);
    }

    private Object getAndLogDefaultValueUsage(@NotNull Object instance) {
        field.fixAccess(true);
        Object defaultValue = field.getValue(instance);
        logger.warn("Using default value: {}", defaultValue);
        return defaultValue;
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

    /**
     * Represents an exception thrown during a failed {@link #validateValue(Class, Object)}
     */
    static final class ViolationException extends RuntimeException {

        /**
         * Instantiates a new Violation exception.
         *
         * @param constraintViolation the constraint violation that triggered the exception
         */
        public ViolationException(final @NotNull ConstraintViolation<?> constraintViolation) {
            super(constraintViolation.getMessage());
        }

    }

}
