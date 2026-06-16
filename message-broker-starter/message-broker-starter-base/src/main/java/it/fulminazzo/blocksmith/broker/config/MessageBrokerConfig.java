package it.fulminazzo.blocksmith.broker.config;

import it.fulminazzo.blocksmith.broker.MessageBroker;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.data.mapper.MapperFormat;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the configuration for a {@link MessageBroker}.
 *
 * @param <C> the type of this configuration
 * @see MessageBrokerFactory
 * @see MessageBroker
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class MessageBrokerConfig<C extends MessageBrokerConfig<C>> {

    @NonNull(exceptionMessage = "'mapper' must be declared")
    private @NotNull MapperFormat mapper = MapperFormat.JSON;

    /**
     * Gets the mapper.
     *
     * @return the mapper
     */
    public @NotNull Mapper getMapper() {
        return mapper.newMapper();
    }

    /**
     * Sets the mapper.
     *
     * @param mapper the mapper
     * @return this object (for method chaining)
     */
    @SuppressWarnings("unchecked")
    public @NotNull C setMapper(final @NotNull MapperFormat mapper) {
        this.mapper = mapper;
        return (C) this;
    }

}
