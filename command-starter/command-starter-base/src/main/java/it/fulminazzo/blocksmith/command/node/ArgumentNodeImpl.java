package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.argument.ArgumentParseException;
import it.fulminazzo.blocksmith.command.argument.ArgumentParser;
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers;
import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.validation.ValidationException;
import it.fulminazzo.blocksmith.validation.Validator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * Base implementation of {@link ArgumentNode}.
 *
 * @param <T> the type that the value is converted to
 */
@SuppressWarnings("unchecked")
@Getter
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ArgumentNodeImpl<T> extends ArgumentNode<T> {
    @Getter(AccessLevel.NONE)
    final @NotNull Parameter parameter;

    /**
     * Instantiates a new Argument node.
     *
     * @param name      the name
     * @param parameter the parameter
     * @param optional  if {@code true} the argument will not be mandatory
     */
    ArgumentNodeImpl(final @NotNull String name,
                     final @NotNull Parameter parameter,
                     final boolean optional) {
        super(name, optional);
        this.parameter = parameter;
    }

    @Override
    protected @Nullable T parseCurrentImpl(final @NotNull InputVisitor<?, ?> visitor) throws ArgumentParseException, ValidationException {
        final @NotNull ArgumentParser<T> parser = getParser();
        T value = parser.parse(visitor);
        Validator.getInstance().validate(parameter, value);
        return value;
    }

    @Override
    public @NotNull ArgumentParser<T> getParser() {
        Type type = parameter.getParameterizedType();
        if (type != null) return ArgumentParsers.of(type);
        else return ArgumentParsers.of(getType());
    }

    @Override
    public @NotNull Class<T> getType() {
        return (Class<T>) Reflect.toWrapper(parameter.getType());
    }

}
