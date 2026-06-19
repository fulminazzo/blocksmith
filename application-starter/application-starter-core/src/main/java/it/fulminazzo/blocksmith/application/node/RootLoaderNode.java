package it.fulminazzo.blocksmith.application.node;

import it.fulminazzo.blocksmith.application.LoaderVisitor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Special implementation of {@link LoaderNode} used to represent the root of the loader tree.
 *
 * @see LoaderNode
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class RootLoaderNode extends LoaderNode {

    @Override
    public <X extends Throwable> void accept(final @NotNull LoaderVisitor<X> visitor) throws X {
        visitor.visitRoot(this);
    }

}
