package it.fulminazzo.blocksmith.broker;

import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperFormat;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract implementation of the {@link MessageBrokerBuilder} interface.
 * Provides common functionality for all builders.
 *
 * @param <B> the type of the message broker
 * @param <A> the type of this builder (for method chaining)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMessageBrokerBuilder<B extends MessageBroker<?>, A extends AbstractMessageBrokerBuilder<B, A>>
        implements MessageBrokerBuilder<B> {
    protected @NotNull Mapper mapper = MapperFormat.JSON.newMapper();

    /**
     * Sets the data mapper.
     * <br>
     * The mapper will be used to serialize messages in a uniform format across platforms.
     * <br>
     * Default: {@link MapperFormat#JSON}
     *
     * @param mapper the mapper
     * @return this object (for method chaining)
     */
    public @NotNull A mapper(final @NotNull Mapper mapper) {
        this.mapper = mapper;
        return (A) this;
    }

}
