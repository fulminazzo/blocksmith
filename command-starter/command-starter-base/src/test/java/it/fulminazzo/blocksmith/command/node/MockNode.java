package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.visitor.InputVisitor;
import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public final class MockNode extends CommandNode implements Cloneable {
    @Getter
    private final @NotNull String name;

    @Override
    public <T, X extends Exception> T accept(final @NotNull Visitor<T, X> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return token.equalsIgnoreCase(name);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull InputVisitor<?, ?> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MockNode clone() {
        return new MockNode(name);
    }

}
