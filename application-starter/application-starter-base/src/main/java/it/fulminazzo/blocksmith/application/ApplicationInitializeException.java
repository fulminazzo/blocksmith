package it.fulminazzo.blocksmith.application;

import lombok.experimental.StandardException;

/**
 * An exception thrown during a failed initialization of the application.
 */
@StandardException
public final class ApplicationInitializeException extends Exception {
    private static final long serialVersionUID = 6819735949224600789L;

}
