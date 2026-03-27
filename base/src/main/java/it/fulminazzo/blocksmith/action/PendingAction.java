package it.fulminazzo.blocksmith.action;

/**
 * Temporarily stores an action for later use.
 */
@FunctionalInterface
public interface PendingAction {

    /**
     * Runs the action.
     */
    void run();

}
