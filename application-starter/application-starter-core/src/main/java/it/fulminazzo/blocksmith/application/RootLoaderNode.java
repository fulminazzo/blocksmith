package it.fulminazzo.blocksmith.application;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Special implementation of {@link LoaderNode} used to represent the root of the loader tree.
 *
 * @see LoaderNode
 */
@Value
@EqualsAndHashCode(callSuper = true)
class RootLoaderNode extends LoaderNode {
    // empty on purpose

}
