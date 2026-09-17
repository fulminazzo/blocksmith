package it.fulminazzo.blocksmith.application;

import it.fulminazzo.blocksmith.reflect.Reflect;
import it.fulminazzo.blocksmith.reflect.ReflectException;
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
     * If the variable is dotted ("variable.subvariable"),
     * it will be resolved recursively (only works for maps and objects).
     *
     * @param key the name of the variable
     * @param <T> the type of the value
     * @return the value
     * @throws ApplicationInitializeException if the variable is not found
     */
    public <T> T get(final @NotNull String key) throws ApplicationInitializeException {
        try {
            return get(environment, key);
        } catch (ApplicationInitializeException e) {
            throw new ApplicationInitializeException(
                    String.format("Could not find variable with path '%s'", key),
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(
            final @NotNull Object object,
            final @NotNull String key
    ) throws ApplicationInitializeException {
        final String firstKey;
        final String remainder;
        if (key.contains(".")) {
            int idx = key.indexOf(".");
            firstKey = key.substring(0, idx);
            remainder = key.substring(idx + 1);
        } else {
            firstKey = key;
            remainder = null;
        }
        T result;
        if (object instanceof Map) {
            result = (T) ((Map<?, ?>) object).get(firstKey);
        } else
            try {
                result = Reflect.on(object).get(firstKey).get();
            } catch (ReflectException e) {
                result = null;
            }
        if (remainder == null) return result;
        else if (result != null) return get(result, remainder);
        else throw new ApplicationInitializeException(String.format(
                    "Could not find '%s' in object of type %s: %s",
                    key, object.getClass().getCanonicalName(), object
            ));
    }

}
