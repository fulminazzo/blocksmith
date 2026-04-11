package it.fulminazzo.blocksmith.message.argument.time.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class LiteralNode extends TimeNode {
    private final @NotNull String text;

    @Override
    protected @NotNull String parseSingle(final long time) {
        return text;
    }

}
