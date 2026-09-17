package it.fulminazzo.blocksmith.application;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Identifies the current context of initialization of the application.
 *
 * @see Application
 * @see FieldInitializerHandler
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(access = AccessLevel.PACKAGE)
public class InitializationContext {
    @NotNull Application application;
    @NotNull Map<String, Object> environment;

    /**
     * Gets the requested environment variable value.
     *
     * @param key the name of the variable
     * @param <T> the type of the value
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final @NotNull String key) {
        return (T) environment.get(key);
    }

}
