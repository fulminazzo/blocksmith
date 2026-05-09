package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a scope for the {@link NodeOrderValidator}.
 */
final class OrderScope {
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
