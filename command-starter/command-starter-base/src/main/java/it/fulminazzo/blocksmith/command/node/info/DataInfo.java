package it.fulminazzo.blocksmith.command.node.info;

import lombok.*;

/**
 * Defines a general information object.
 *
 * @param <I> the type of this object
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class DataInfo<I extends DataInfo<I>> {
    /**
     * If <code>true</code>, the data of this object was automatically computed.
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private boolean autoComputed;

    /**
     * Fills any missing data from this information object
     * with the data from the given information object.
     * <br>
     * If {@link #isAutoComputed()}, the data will be overwritten.
     *
     * @param info the reference information object
     */
    public abstract void merge(final @NonNull I info);

}
