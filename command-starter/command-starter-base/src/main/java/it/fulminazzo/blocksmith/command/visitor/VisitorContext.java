package it.fulminazzo.blocksmith.command.visitor;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Contains the current context that a {@link it.fulminazzo.blocksmith.command.visitor.Visitor} should use.
 */
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class VisitorContext {
    //TODO: application
    //TODO: registry
    //TODO: sender

}
