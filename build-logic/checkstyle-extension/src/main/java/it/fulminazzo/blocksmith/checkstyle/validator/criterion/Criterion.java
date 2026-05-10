package it.fulminazzo.blocksmith.checkstyle.validator.criterion;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.jetbrains.annotations.NotNull;

/**
 * A general object representing a criterion.
 */
public interface Criterion {

    /**
     * Checks if the node matches the current criterion.
     *
     * @param node the node to check
     * @return {@code true} if the node matches, {@code false} otherwise
     */
    boolean matches(final @NotNull DetailAST node);

    /**
     * Gets the error message in case the node should have matched
     * with this criterion, but did not.
     *
     * @return the error message
     */
    @NotNull String getErrorMessage();

}
