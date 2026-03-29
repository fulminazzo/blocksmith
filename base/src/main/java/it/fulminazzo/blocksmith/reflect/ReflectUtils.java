package it.fulminazzo.blocksmith.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static it.fulminazzo.blocksmith.reflect.Reflect.toWrapper;

/**
 * A collection of utilities for working with reflections.
 */
final class ReflectUtils {

    /**
     * Checks if the given class extends the type.
     *
     * @param clazz the class
     * @param type  the type
     * @return <code>true</code> if it does
     */
    public static boolean extendsType(final @NotNull Class<?> clazz, final @NotNull Type type) {
        Class<?> current = clazz;
        if (typeMatches(current, type)) return true;
        while (current != null) {
            Type genericSuper = current.getGenericSuperclass();
            if (genericSuper != null && typeMatches(genericSuper, type)) return true;
            for (Type interfaceType : current.getGenericInterfaces())
                if (extendsType(toClass(interfaceType), type)) return true;
            current = current.getSuperclass();
        }
        return false;
    }

    /**
     * Compares the two given types to verify if they match or not at runtime.
     *
     * @param source the source type. This comparison assumes that "source" represents
     *               a concrete type referred to an object.
     *               Because of this, the {@link WildcardType}, {@link TypeVariable}
     *               and {@link GenericArrayType} should never be encountered.
     * @param target the target type
     * @return <code>true</code> if they match
     */
    static boolean typeMatches(final @NotNull Type source, final @NotNull Type target) {
        final Class<?> sourceClass = toWrapper(toClass(source));
        if (target instanceof Class<?>)
            return toWrapper(((Class<?>) target)).isAssignableFrom(sourceClass);
        else if (target instanceof ParameterizedType) {
            ParameterizedType targetParameterizedType = (ParameterizedType) target;
            if (!(source instanceof ParameterizedType))
                return typeMatches(source, targetParameterizedType.getRawType());
            ParameterizedType sourceParameterizedType = (ParameterizedType) source;
            if (!typeMatches(sourceParameterizedType.getRawType(), targetParameterizedType.getRawType())) return false;
            Type[] sourceActualTypeArguments = sourceParameterizedType.getActualTypeArguments();
            Type[] targetActualTypeArguments = targetParameterizedType.getActualTypeArguments();
            for (int i = 0; i < sourceActualTypeArguments.length; i++) {
                if (!typeMatches(sourceActualTypeArguments[i], targetActualTypeArguments[i])) return false;
            }
            return true;
        } else if (target instanceof TypeVariable<?>) {
            TypeVariable<?> targetTypeVariable = (TypeVariable<?>) target;
            return Arrays.stream(targetTypeVariable.getBounds()).allMatch(b -> typeMatches(source, b));
        } else if (target instanceof GenericArrayType) {
            GenericArrayType targetGenericArrayType = (GenericArrayType) target;
            if (!sourceClass.isArray()) return false;
            Type targetComponentType = targetGenericArrayType.getGenericComponentType();
            Type sourceComponentType = sourceClass.getComponentType();
            return typeMatches(sourceComponentType, targetComponentType);
        } else if (target instanceof WildcardType) {
            WildcardType targetWildcardType = (WildcardType) target;
            return Arrays.stream(targetWildcardType.getUpperBounds()).allMatch(b -> typeMatches(source, b)) &&
                    Arrays.stream(targetWildcardType.getLowerBounds()).allMatch(b -> typeMatches(b, source));
        } else return false;
    }

    /**
     * Converts the given type to a {@link Class}.
     *
     * @param type the type
     * @return the class
     */
    public static @NotNull Class<?> toClass(final @NotNull Type type) {
        if (type instanceof Class<?>) return (Class<?>) type;
        else if (type instanceof ParameterizedType) return toClass(((ParameterizedType) type).getRawType());
        else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            Type[] bounds = typeVariable.getBounds();
            return bounds.length > 0 ? toClass(bounds[0]) : Object.class;
        } else if (type instanceof GenericArrayType) {
            Class<?> componentType = toClass(((GenericArrayType) type).getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length > 0) return toClass(lowerBounds[0]);
            else return toClass(wildcardType.getUpperBounds()[0]);
        } else throw new IllegalArgumentException("Unsupported type: " + type);
    }

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

    /**
     * Regroups the given values to create an array that matches the expected parameter types.
     *
     * @param parameters      the parameters
     * @param parameterValues the parameter values
     * @return the array
     */
    static @Nullable Object @NotNull [] regroup(final @NotNull Parameter @NotNull [] parameters,
                                                final @Nullable Object @NotNull [] parameterValues) {
        List<Object> flattened = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            int remaining = parameterValues.length - i;
            if (remaining == 1) {
                Type type = parameters[i].getParameterizedType();
                Object value = parameterValues[i];
                if (value != null && typeMatches(value.getClass(), type)) {
                    flattened.add(value);
                    break;
                }
            }
            Object[] array = new Object[remaining];
            System.arraycopy(parameterValues, i, array, 0, remaining);
            flattened.add(array);
        }
        return flattened.toArray();
    }

    /**
     * Verifies that the given parameter types match the parameters.
     *
     * @param parameters the parameters
     * @param given      the parameter types
     * @return <code>true</code> if they match
     */
    static boolean parameterMatches(final @NotNull Parameter @NotNull [] parameters,
                                    final @Nullable Class<?> @NotNull [] given) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Type type = parameter.getParameterizedType();
            if (i >= given.length) return parameter.isVarArgs();
            Class<?> current = given[i];
            if (parameter.isVarArgs()) {
                Type componentType = type instanceof Class<?>
                        ? ((Class<?>) type).getComponentType()
                        : ((GenericArrayType) type).getGenericComponentType();
                for (int j = i; j < given.length; j++) {
                    current = given[j];
                    if (current != null && !typeMatches(current, componentType)) return false;
                }
                return true;
            }
            if (current != null && !typeMatches(current, type)) return false;
        }
        return given.length == parameters.length;
    }

}
