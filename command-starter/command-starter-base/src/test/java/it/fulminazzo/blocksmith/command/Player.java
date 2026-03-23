package it.fulminazzo.blocksmith.command;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Player extends CommandSender {

    public Player(final @NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Player addPermissions(final String @NotNull ... permissions) {
        return (Player) super.addPermissions(permissions);
    }

    @Override
    public @NotNull Player setOp(final boolean op) {
        return (Player) super.setOp(op);
    }

}
