package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A collection of utilities for working with reflections.
 */
final class ReflectUtils {

    /**
     * Prints out the type in a human-readable format.
     *
     * @param type the Java type
     * @return the string representation of the type
     */
    public static @NotNull String toString(final @NotNull Type type) {
        if (type instanceof Class<?>) return ((Class<?>) type).getCanonicalName();
        else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return String.format("%s<%s>",
                    toString(parameterizedType.getRawType()),
                    Arrays.stream(parameterizedType.getActualTypeArguments())
                            .map(ReflectUtils::toString)
                            .collect(Collectors.joining(", "))
            );
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            String string = typeVariable.getName();
            String bounds = Arrays.stream(typeVariable.getBounds())
                    .filter(b -> !b.equals(Object.class))
                    .map(ReflectUtils::toString)
                    .collect(Collectors.joining(" & "));
            if (!bounds.isEmpty()) string += " extends " + bounds;
            return string;
        } else if (type instanceof GenericArrayType)
            return toString(((GenericArrayType) type).getGenericComponentType()) + "[]";
        else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            String string = "?";

            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length > 0) {
                Type lowerBound = lowerBounds[0];
                if (!lowerBound.equals(Object.class))
                    string += " super " + toString(lowerBound);
            }
            
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length > 0) {
                Type upperBound = upperBounds[0];
                if (!upperBound.equals(Object.class))
                    string += " extends " + toString(upperBound);
            }

            return string;
        } else throw new IllegalArgumentException("Unsupported type: " + type);
    }

}
