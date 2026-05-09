package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.Criterion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * An object to validate nodes against multiple ordered rulesets.
 *
 * @see Criterion
 */
public final class NodeOrderValidator implements NodeValidator, NodeScorer {
    private final @NotNull List<NodeScorerImpl> scorers = new ArrayList<>();
    private final @NotNull Deque<OrderScope> scopes = new ArrayDeque<>();
    private @Nullable NodeValidator next;

    /**
     * Instantiates a new Node order validator.
     *
     * @param criteria the group of criteria to use that represent the different rulesets
     */
    public NodeOrderValidator(final @NotNull Criterion @NotNull [] @NotNull ... criteria) {
        for (Criterion[] vs : criteria)
            scorers.add(new NodeScorerImpl(vs));
    }

    /**
     * Updates the last computed score.
     *
     * @param lastScore the new score
     */
    public void setLastScore(final int lastScore) {
        getLastScope().setLastScore(lastScore);
    }

    /**
     * Gets the latest score of {@link #computeScore(DetailAST)}.
     *
     * @return the last score
     */
    public int getLastScore() {
        return getLastScope().getLastScore();
    }

    /**
     * Enters a new computation scope.
     * Scopes are completely independent of each other:
     * updating a {@link #getLastScore()} will not affect the score of other scopes.
     */
    public void enterScope() {
        scopes.push(new OrderScope());
    }

    /**
     * Exits the current computation scope.
     */
    public void exitScope() {
        if (!scopes.isEmpty()) scopes.pop();
    }

    @SuppressWarnings("DataFlowIssue")
    private @NotNull OrderScope getLastScope() {
        if (scopes.isEmpty()) enterScope();
        return scopes.peek();
    }

    @Override
    public void validateNode(final @NotNull DetailAST node) throws ValidationException {
        int score = computeScore(node);

        int totalBits = 0;
        for (NodeScorerImpl scorer : scorers) totalBits += getRankerOffset(scorer);

        for (NodeScorerImpl scorer : scorers) {
            int offset = getRankerOffset(scorer);
            int mask = offset;
            if (mask != 1) mask++;

            int bits = totalBits - offset;
            int last = (getLastScore() >> bits) & mask;
            int current = (score >> bits) & mask;

            if (current < last)
                throw new ValidationException(scorer.getValidator(current).getErrorMessage());
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
        for (NodeScorerImpl scorer : scorers) {
            int offset = getRankerOffset(scorer);
            score <<= offset;
            score += scorer.computeScore(node);
        }
        return score;
    }

    private static int getRankerOffset(final @NotNull NodeScorerImpl scorer) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(scorer.getValidatorsCount() - 1);
    }

}
