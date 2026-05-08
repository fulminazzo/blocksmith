package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checker.validator.RankerValidator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An object to validate nodes against multiple ordered rulesets.
 *
 * @see RankerValidator
 */
public final class NodeValidator implements Ranker {
    private final @NotNull List<RankerImpl> rankers = new ArrayList<>();
    private int lastScore;

    /**
     * Instantiates a new Node validator.
     *
     * @param validators the group of validators to use that represent the different rulesets
     */
    public NodeValidator(final @NotNull RankerValidator @NotNull [] @NotNull ... validators) {
        for (RankerValidator[] vs : validators)
            rankers.add(new RankerImpl(vs));
    }

    /**
     * Validates the given node against the previously recorded score.
     *
     * @param node the node to validate
     * @throws ValidationException if the node ordering is not valid
     */
    public void validateNode(final @NotNull DetailAST node) throws ValidationException {
        int score = computeScore(node);

        int totalBits = 0;
        for (RankerImpl ranker : rankers) totalBits += getRankerOffset(ranker);

        for (RankerImpl ranker : rankers) {
            int offset = getRankerOffset(ranker);
            int mask = offset;
            if (mask != 1) mask++;

            int bits = totalBits - offset;
            int last = (lastScore >> bits) & mask;
            int current = (score >> bits) & mask;

            if (current < last)
                throw new ValidationException(ranker.getValidator(offset).getErrorMessage());
            else if (current > last) {
                lastScore = score;
                break;
            }

            totalBits -= offset;
        }
    }

    /**
     * Resets the validator.
     */
    public void reset() {
        lastScore = 0;
    }

    @Override
    public int computeScore(final @NotNull DetailAST node) {
        int score = 0;
        for (RankerImpl ranker : rankers) {
            int offset = getRankerOffset(ranker);
            score <<= offset;
            score += ranker.computeScore(node);
        }
        return score;
    }

    private static int getRankerOffset(final @NotNull RankerImpl ranker) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(ranker.getValidatorsCount() - 1);
    }

}
