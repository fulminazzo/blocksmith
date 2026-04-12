package it.fulminazzo.blocksmith.command.node.handler;

import it.fulminazzo.blocksmith.structure.cooldown.FixedCooldownManager;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Handler to actually execute the command upon successful arguments validation.
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class ExecutionHandler {
    private final @NotNull CommandExecutor executor;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private @Nullable FixedCooldownManager<?> cooldownManager;

    /**
     * Sets a time to wait before executing the command again.
     *
     * @param cooldown the cooldown (<code>null</code> to disable)
     * @return this object (for method chaining)
     */
    public @NotNull ExecutionHandler setCooldown(final @Nullable Duration cooldown) {
        if (cooldown == null) cooldownManager = null;
        else cooldownManager = new FixedCooldownManager<>(cooldown);
        return this;
    }

}
