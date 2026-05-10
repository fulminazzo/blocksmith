package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.Criterion;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an object capable of ranking nodes based on certain criteria.
 *
 * @see Criterion
 */
interface NodeScorer {

    /**
     * Computes a score for the given node.
     *
     * @param node the node
     * @return the score of the node
     */
    int computeScore(final @NotNull DetailAST node);

}
