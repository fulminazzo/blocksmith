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
    private final @NotNull Criterion @NotNull [] validators;

    /**
     * Gets the validator at the given index.
     *
     * @param index the index of the validator
     * @return the validator
     */
    public @NotNull Criterion getValidator(final int index) {
        return validators[index];
    }

    /**
     * Gets the number of validators.
     *
     * @return the number of validators`
     */
    public int getValidatorsCount() {
        return validators.length;
    }

    @Override
    public int computeScore(final @NotNull DetailAST node) {
        for (int i = 0; i < getValidatorsCount(); i++) {
            Criterion validator = validators[i];
            if (validator.matches(node)) return i;
        }
        throw new IllegalArgumentException(String.format(
                "Could not compute score of node '%s' with validators: %s",
                node, Arrays.toString(validators)
        ));
    }

}
