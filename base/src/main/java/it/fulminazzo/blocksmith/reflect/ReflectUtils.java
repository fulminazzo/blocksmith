package it.fulminazzo.blocksmith.reflect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static it.fulminazzo.blocksmith.reflect.Reflect.toWrapper;

/**
 * A collection of utilities for working with reflections.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectUtils {

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
            return toClass(bounds[0]);
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
            Type upperBound = upperBounds[0];
            if (!upperBound.equals(Object.class))
                string += " extends " + toString(upperBound);

            return string;
        } else throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Checks if the given class extends the type.
     *
     * @param clazz the class
     * @param type  the type
     * @return {@code true} if it does
     */
    static boolean extendsType(final @NotNull Class<?> clazz, final @NotNull Type type) {
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
     * @return {@code true} if they match
     */
    static boolean typeMatches(final @NotNull Type source, final @NotNull Type target) {
        return scoreTypeMatching(source, target).isPresent();
    }

    private static @NotNull OptionalInt scoreTypeMatching(final @NotNull Type source, final @NotNull Type target) {
        final Class<?> sourceClass = toWrapper(toClass(source));
        if (target instanceof Class<?>) {
            Class<?> targetClass = toWrapper((Class<?>) target);
            if (targetClass.isAssignableFrom(sourceClass))
                return OptionalInt.of(-hierarchyDistance(sourceClass, targetClass));
            else return OptionalInt.empty();
        } else if (target instanceof ParameterizedType) {
            ParameterizedType targetParameterizedType = (ParameterizedType) target;
            if (!(source instanceof ParameterizedType))
                return scoreTypeMatching(source, targetParameterizedType.getRawType());
            ParameterizedType sourceParameterizedType = (ParameterizedType) source;
            OptionalInt score = scoreTypeMatching(sourceParameterizedType.getRawType(), targetParameterizedType.getRawType());
            if (score.isEmpty()) return score;
            int s = score.getAsInt();
            Type[] sourceActualTypeArguments = sourceParameterizedType.getActualTypeArguments();
            Type[] targetActualTypeArguments = targetParameterizedType.getActualTypeArguments();
            for (int i = 0; i < sourceActualTypeArguments.length; i++) {
                score = scoreTypeMatching(sourceActualTypeArguments[i], targetActualTypeArguments[i]);
                if (score.isEmpty()) return score;
                s += score.getAsInt();
            }
            return OptionalInt.of(s);
        } else if (target instanceof TypeVariable<?>) {
            TypeVariable<?> targetTypeVariable = (TypeVariable<?>) target;
            return aggregateScore(source, targetTypeVariable.getBounds());
        } else if (target instanceof GenericArrayType) {
            GenericArrayType targetGenericArrayType = (GenericArrayType) target;
            if (!sourceClass.isArray()) return OptionalInt.empty();
            Type targetComponentType = targetGenericArrayType.getGenericComponentType();
            Type sourceComponentType = sourceClass.getComponentType();
            return scoreTypeMatching(sourceComponentType, targetComponentType);
        } else if (target instanceof WildcardType) {
            WildcardType targetWildcardType = (WildcardType) target;
            OptionalInt upper = aggregateScore(source, targetWildcardType.getUpperBounds());
            if (upper.isEmpty()) return upper;
            int score = upper.getAsInt();
            for (Type b : targetWildcardType.getLowerBounds()) {
                OptionalInt s = scoreTypeMatching(b, source);
                if (s.isEmpty()) return s;
                score += s.getAsInt();
            }
            return OptionalInt.of(score);
        } else return OptionalInt.empty();
    }

    private static @NotNull OptionalInt aggregateScore(final @NotNull Type source, final @NotNull Type @NotNull [] bounds) {
        int score = 0;
        for (Type b : bounds) {
            OptionalInt opt = scoreTypeMatching(source, b);
            if (opt.isEmpty()) return opt;
            else score += opt.getAsInt();
        }
        return OptionalInt.of(score);
    }

    private static int hierarchyDistance(final @NotNull Class<?> source, final @NotNull Class<?> target) {
        if (source.equals(target)) return 0;
        Queue<Class<?>> queue = new LinkedList<>();
        Map<Class<?>, Integer> distances = new HashMap<>();
        queue.add(source);
        distances.put(source, 0);
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            int distance = distances.get(current);

            List<Class<?>> parents = new ArrayList<>();
            Class<?> superClass = current.getSuperclass();
            if (superClass != null) parents.add(superClass);
            parents.addAll(Arrays.asList(current.getInterfaces()));
            for (Class<?> p : parents)
                if (!distances.containsKey(p)) {
                    int d = distance + 1;
                    if (p.equals(target)) return d;
                    distances.put(p, d);
                    queue.add(p);
                }
        }
        return Integer.MAX_VALUE; // fallback value in case of target = Object and source = Interface
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
            Parameter parameter = parameters[i];
            if (parameter.isVarArgs()) {
                List<Object> remaining = new ArrayList<>(Arrays.asList(parameterValues).subList(i, parameterValues.length));
                Class<?> type = parameter.getType();
                if (remaining.size() == 1) {
                    Object last = remaining.get(0);
                    if (last != null && extendsType(last.getClass(), type)) {
                        flattened.add(last);
                        continue;
                    }
                }
                flattened.add(remaining.toArray((Object[]) Array.newInstance(type.getComponentType(), remaining.size())));
                continue;
            }
            flattened.add(parameterValues[i]);
        }
        return flattened.toArray();
    }

    /**
     * Finds the executable that best matches with the given parameter types.
     *
     * @param <E>            the type of the executable
     * @param executables    the collection to find the executable from
     * @param parameterTypes the parameter types
     * @return the executable (if found)
     */
    static <E extends Executable> @NotNull Optional<E> findExecutable(final @NotNull Collection<E> executables,
                                                                      final @Nullable Class<?> @NotNull [] parameterTypes) {
        return executables.stream()
                .map(e -> {
                    OptionalInt score = parameterMatches(e.getParameters(), parameterTypes);
                    return score.isPresent() ? Map.entry(e, score.getAsInt()) : null;
                })
                .filter(Objects::nonNull)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Verifies that the given parameter types match the parameters.
     *
     * @param parameters the parameters
     * @param given      the parameter types
     * @return the parameters compatibility, computed as the difference between the given and the requested
     */
    static OptionalInt parameterMatches(final @NotNull Parameter @NotNull [] parameters,
                                        final @Nullable Class<?> @NotNull [] given) {
        int total = 0;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Type type = parameter.getParameterizedType();
            if (parameter.isVarArgs()) {
                Type componentType = type instanceof Class<?>
                        ? ((Class<?>) type).getComponentType()
                        : ((GenericArrayType) type).getGenericComponentType();
                total -= 1;
                for (int j = i; j < given.length; j++) {
                    Class<?> current = given[j];
                    if (current == null) continue;
                    OptionalInt score = scoreTypeMatching(current, componentType);
                    if (score.isEmpty()) return score;
                    total += score.getAsInt();
                }
                return OptionalInt.of(total);
            }
            if (i >= given.length) return OptionalInt.empty();
            Class<?> current = given[i];
            if (current == null) continue;
            OptionalInt score = scoreTypeMatching(current, type);
            if (score.isEmpty()) return score;
            total += score.getAsInt();
        }
        if (given.length != parameters.length) return OptionalInt.empty();
        else return OptionalInt.of(total);
    }

}
