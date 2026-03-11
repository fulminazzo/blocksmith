package it.fulminazzo.blocksmith.structure;

import lombok.*;

/**
 * A simple structure that holds two objects.
 *
 * @param <F> the type of the first object
 * @param <S> the type of the second object
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Pair<F, S> {
    F first;
    S second;

    /**
     * Creates a new Pair.
     *
     * @param <F>    the type of the first object
     * @param <S>    the type of the second object
     * @param first  the first object
     * @param second the second object
     * @return the pair
     */
    public static <F, S> Pair<F, S> of(final F first, final S second) {
        return new Pair<>(first, second);
    }

}
