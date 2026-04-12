package it.fulminazzo.blocksmith.command.visitor;

import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Contains the current context that a {@link it.fulminazzo.blocksmith.command.visitor.Visitor} should use.
 */
@Getter
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public abstract class VisitorContext {
    //TODO: application
    //TODO: registry
    @NotNull CommandSenderWrapper<?> commandSender;

}
