package it.fulminazzo.blocksmith.validation;

import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.temporal.ChronoField;
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
    static long toMillis(final @NotNull Object object) {
        if (object instanceof Date) return ((Date) object).getTime() / 1000;
        else if (object instanceof Calendar) return ((Calendar) object).getTimeInMillis() / 1000;
        else {
            TemporalAccessor time = (TemporalAccessor) object;
            if (time.isSupported(ChronoField.INSTANT_SECONDS)) return time.getLong(ChronoField.INSTANT_SECONDS);
            else {
                ZoneId zone = ZoneId.systemDefault();
                ZoneOffset offset = zone.getRules().getOffset(Instant.now());
                if (time.isSupported(ChronoField.EPOCH_DAY))
                    if (time.isSupported(ChronoField.NANO_OF_DAY))
                        return LocalDateTime.from(time).toEpochSecond(offset);
                    else return LocalDate.from(time).toEpochSecond(LocalTime.now(), offset);
                else return LocalTime.from(time).toEpochSecond(LocalDate.now(), offset);
            }
        }
    }

}
