package it.fulminazzo.blocksmith.checker;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checker.validator.RankerValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An object to validate nodes against multiple ordered rulesets.
 *
 * @see RankerValidator
 */
public final class NodeOrderValidator implements NodeValidator, Ranker {
    private final @NotNull List<RankerImpl> rankers = new ArrayList<>();
    private final @NotNull Deque<Integer> scopes = new ArrayDeque<>();
    private @Nullable NodeValidator next;

    /**
     * Instantiates a new Node validator.
     *
     * @param validators the group of validators to use that represent the different rulesets
     */
    public NodeOrderValidator(final @NotNull RankerValidator @NotNull [] @NotNull ... validators) {
        for (RankerValidator[] vs : validators)
            rankers.add(new RankerImpl(vs));
    }

    /**
     * Updates the last computed score.
     *
     * @param lastScore the new score
     */
    public void setLastScore(final int lastScore) {
        exitScope();
        scopes.push(lastScore);
    }

    /**
     * Gets the latest score of {@link #computeScore(DetailAST)}.
     *
     * @return the last score
     */
    public int getLastScore() {
        return scopes.isEmpty() ? 0 : scopes.peek();
    }

    /**
     * Enters a new computation scope.
     * Scopes are completely independent of each other:
     * updating a {@link #getLastScore()} will not affect the score of other scopes.
     */
    public void enterScope() {
        scopes.push(0);
    }

    /**
     * Exits the current computation scope.
     */
    public void exitScope() {
        if (!scopes.isEmpty()) scopes.pop();
    }

    @Override
    public void validateNode(final @NotNull DetailAST node) throws ValidationException {
        int score = computeScore(node);

        int totalBits = 0;
        for (RankerImpl ranker : rankers) totalBits += getRankerOffset(ranker);

        for (RankerImpl ranker : rankers) {
            int offset = getRankerOffset(ranker);
            int mask = offset;
            if (mask != 1) mask++;

            int bits = totalBits - offset;
            int last = (getLastScore() >> bits) & mask;
            int current = (score >> bits) & mask;

            if (current < last)
                throw new ValidationException(ranker.getValidator(current).getErrorMessage());
            else if (current > last) {
                setLastScore(score);
                break;
            }

            totalBits -= offset;
        }
    }

    @Override
    public @NotNull NodeOrderValidator then(final @NotNull NodeValidator validator) {
        this.next = validator;
        return this;
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
