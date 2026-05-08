package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Base implementation of {@link Ranker}.
 *
 * @see RankerValidator
 */
@RequiredArgsConstructor
final class RankerImpl implements Ranker {
    private final @NotNull RankerValidator @NotNull [] validators;

    @Override
    public int computeScore(final @NotNull DetailAST node) {
        for (int i = 0; i < getValidatorsCount(); i++) {
            RankerValidator validator = validators[i];
            if (validator.validate(node)) return i;
        }
        throw new IllegalArgumentException(String.format(
                "Could not compute score of node '%s' with validators: %s",
                node, Arrays.toString(validators)
        ));
    }

    /**
     * Gets the number of validators.
     *
     * @return the number of validators`
     */
    public int getValidatorsCount() {
        return validators.length;
    }

}
