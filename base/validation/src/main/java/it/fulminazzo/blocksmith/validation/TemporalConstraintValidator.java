package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

/**
 * A Constraint validator for {@link TemporalAccessor} and time related types.
 */
final class TemporalConstraintValidator extends ConstraintValidatorImpl {

    /**
     * Instantiates a new Temporal constraint validator.
     *
     * @param validPredicate the valid predicate
     */
    public TemporalConstraintValidator(final @NotNull Predicate<@NotNull Long> validPredicate) {
        super(o -> o == null || validPredicate.test(toMillis(o)), Date.class, Calendar.class, TemporalAccessor.class);
    }

    @Override
    public @NotNull String getTypeNames() {
        return "time";
    }

    /**
     * Converts the given object to milliseconds.
     * <br>
     * Supports:
     * <ul>
     *     <li>{@link Date}</li>
     *     <li>{@link Calendar}</li>
     *     <li>any {@link TemporalAccessor} implementation</li>
     * </ul>
     *
     * @param object the object
     * @return the milliseconds
     */
    static @NotNull Long toMillis(final @NotNull Object object) {
        if (object instanceof Date) return ((Date) object).getTime();
        else if (object instanceof Calendar) return ((Calendar) object).getTimeInMillis();
        else return Instant.from((TemporalAccessor) object).toEpochMilli();
    }

}
