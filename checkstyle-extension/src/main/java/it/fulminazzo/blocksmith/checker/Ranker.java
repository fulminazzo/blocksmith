package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an object capable of ranking nodes based on certain criteria.
 *
 * @see RankerValidator
 */
interface Ranker {

    /**
     * Computes a score for the given node.
     *
     * @param node the node
     * @return the score of the node
     */
    int computeScore(final @NotNull DetailAST node);

}
