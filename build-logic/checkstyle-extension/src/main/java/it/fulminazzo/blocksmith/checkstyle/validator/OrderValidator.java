package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.Criterion;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An object to validate nodes against multiple ordered rulesets.
 *
 * @see Criterion
 */
public final class OrderValidator implements Validator, NodeScorer {
    private final @NotNull List<NodeScorerImpl> scorers = new ArrayList<>();
    private final @NotNull Deque<Scope> scopes = new ArrayDeque<>();
    private @Nullable Validator next;

    /**
     * Instantiates a new Node order validator.
     *
     * @param criteria the group of criteria to use that represent the different rulesets
     */
    public OrderValidator(final @NotNull Criterion @NotNull [] @NotNull ... criteria) {
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
     * Gets the maximum possible score.
     *
     * @return the max score
     */
    int getMaxScore() {
        int score = 0;
        for (NodeScorerImpl scorer : scorers) {
            int offset = getRankerOffset(scorer);
            score <<= offset;
            score += scorer.getCriteriaCount() - 1;
        }
        return score;
    }

    @SuppressWarnings("DataFlowIssue")
    private @NotNull Scope getLastScope() {
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
            int mask = (1 << offset) - 1;

            int bits = totalBits - offset;
            int last = (getLastScore() >> bits) & mask;
            int current = (score >> bits) & mask;

            if (current < last)
                throw new ValidationException(node, scorer.getCriterion(current).getErrorMessage())
                        .addArgument(String.join(", ",
                                Arrays.stream(scorer.getCriteria()).map(Object::toString).toArray(String[]::new))
                        );
            else if (current > last) {
                setLastScore(score);
                break;
            }

            totalBits -= offset;
        }
        getLastScope().registerCommonScore(node, score);
    }

    @Override
    public @NotNull OrderValidator then(final @NotNull Validator validator) {
        if (next != null) next.then(validator);
        else this.next = validator;
        return this;
    }

    @Override
    public void enterScope() {
        scopes.push(new Scope());
    }

    @Override
    public void exitScope() throws CompositeValidationException {
        if (!scopes.isEmpty()) {
            Scope scope = scopes.pop();
            if (next == null) return;
            List<ValidationException> exceptions = new ArrayList<>();
            for (int i = 0; i < getMaxScore(); i++) {
                List<DetailAST> nodes = scope.getCommonScores(i);
                next.enterScope();
                for (DetailAST node : nodes)
                    try {
                        next.validateNode(node);
                    } catch (ValidationException e) {
                        exceptions.add(e);
                    }
                try {
                    next.exitScope();
                } catch (CompositeValidationException c) {
                    exceptions.addAll(c.getExceptions());
                }
            }
            if (!exceptions.isEmpty()) throw new CompositeValidationException(exceptions);
        }
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
        return Integer.SIZE - Integer.numberOfLeadingZeros(scorer.getCriteriaCount() - 1);
    }

    /**
     * Represents a scope for {@link OrderValidator}.
     */
    private static final class Scope {
        private final @NotNull Map<Integer, List<DetailAST>> commonScores = new HashMap<>();
        @Getter
        @Setter
        private int lastScore;

        /**
         * Registers a node with the same score.
         *
         * @param node  the node
         * @param score the score
         */
        public void registerCommonScore(final @NotNull DetailAST node, final int score) {
            getCommonScores(score).add(node);
        }

        /**
         * Gets all the registered nodes with the same score.
         *
         * @param score the score
         * @return the common scores
         */
        public @NotNull List<DetailAST> getCommonScores(final int score) {
            return commonScores.computeIfAbsent(score, k -> new ArrayList<>());
        }

    }

}
