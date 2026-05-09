package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * An object to validate executable nodes for their overload ordering.
 *
 * @see Executable
 */
public final class OverloadValidator extends AbstractValidator<OverloadValidator.Scope, OverloadValidator> {

    @Override
    protected void validateNodeImpl(final @NotNull DetailAST node) throws ValidationException {
        Executable executable = Executable.of(node);
        Scope lastScope = getLastScope();
        String name = executable.getName();

        if (name.equals(lastScope.getCurrentGroupName())) {
            Executable lastExecutable = lastScope.getLastExecutable();
            if (lastExecutable != null && executable.compareTo(lastExecutable) < 0)
                throw new ValidationException(node, "executable.overload");
        } else {
            lastScope.closeCurrentGroup();
            if (lastScope.isGroupClosed(name))
                throw new ValidationException(node, "executable.overload.grouped");
            lastScope.setCurrentGroupName(name);
        }

        lastScope.setLastExecutable(executable);
    }

    @Override
    protected @NotNull Scope newScope() {
        return new Scope();
    }

    /**
     * Represents a scope for {@link OverloadValidator}.
     */
    protected static final class Scope extends AbstractValidator.Scope {
        @Getter
        @Setter
        private @Nullable Executable lastExecutable;
        @Getter
        @Setter
        private @Nullable String currentGroupName;
        private final @NotNull Set<String> closedGroups = new HashSet<>();

        /**
         * Closes the current group, recording its name so it cannot reopen.
         */
        public void closeCurrentGroup() {
            if (currentGroupName != null) {
                closedGroups.add(currentGroupName);
                currentGroupName = null;
            }
        }

        /**
         * Returns {@code true} if the given name belongs to a group that was
         * already opened and then closed by a different method appearing after it.
         *
         * @param name the method name to check
         * @return {@code true} if the group is closed
         */
        public boolean isGroupClosed(final @NotNull String name) {
            return closedGroups.contains(name);
        }

    }

}
