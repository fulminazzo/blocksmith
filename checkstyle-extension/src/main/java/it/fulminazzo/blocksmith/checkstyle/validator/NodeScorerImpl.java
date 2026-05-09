package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.Criterion;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Base implementation of {@link NodeScorer}.
 *
 * @see Criterion
 */
@RequiredArgsConstructor
final class NodeScorerImpl implements NodeScorer {
    private final @NotNull Criterion @NotNull [] criteria;

    /**
     * Gets the criterion at the given index.
     *
     * @param index the index of the criterion
     * @return the criterion
     */
    public @NotNull Criterion getCriterion(final int index) {
        return criteria[index];
    }

    /**
     * Gets the number of criteria.
     *
     * @return the number of criteria
     */
    public int getCriteriaCount() {
        return criteria.length;
    }

    @Override
    public int computeScore(final @NotNull DetailAST node) {
        for (int i = 0; i < getCriteriaCount(); i++) {
            Criterion criterion = criteria[i];
            if (criterion.matches(node)) return i;
        }
        throw new IllegalArgumentException(String.format(
                "Could not compute score of node '%s' with criteria: %s",
                node, Arrays.toString(criteria)
        ));
    }

}
