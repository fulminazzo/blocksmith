package it.fulminazzo.blocksmith.command.node;

import it.fulminazzo.blocksmith.command.visitor.Visitor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MockNode extends CommandNode implements Cloneable {
    @NotNull String name;

    @Override
    public <T, X extends Exception> T accept(final @NotNull Visitor<T, X> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean matches(final @NotNull String token) {
        return token.equalsIgnoreCase(name);
    }

    @Override
    public @NotNull List<String> getCompletions(final @NotNull Visitor<?, ?> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MockNode clone() {
        return new MockNode(name);
    }

}
