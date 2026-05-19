package it.fulminazzo.blocksmith.checkstyle.validator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

/**
 * A special exception to combine several {@link ValidationException}s.
 * For internal use only.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class CompositeValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = 8328829751405869982L;

    private final @NotNull List<ValidationException> exceptions;

}
