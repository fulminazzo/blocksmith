package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.validation.annotation.*;
import it.fulminazzo.blocksmith.validation.annotation.Character;

import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("unused")
final class Fields {

    @NonNull
    private Object nonNull;
    @AssertFalse
    private boolean assertFalse;
    @AssertTrue
    private boolean assertTrue;
    @Max(0)
    private int max;
    @Max(0)
    private Duration maxDuration;
    @MaxChar('Z')
    private char maxCharacter;
    @NegativeOrZero
    private int negativeOrZero;
    @NegativeOrZero
    private Duration negativeOrZeroDuration;
    @Negative
    private int negative;
    @Negative
    private Duration negativeDuration;
    @Min(0)
    private int min;
    @Min(0)
    private Duration minDuration;
    @MinChar('A')
    private char minCharacter;
    @PositiveOrZero
    private int positiveOrZero;
    @PositiveOrZero
    private Duration positiveOrZeroDuration;
    @Positive
    private int positive;
    @Positive
    private Duration positiveDuration;
    @Range(min = 0.5, max = 10.5)
    private int range;
    @Range(min = 0.5, max = 10.5)
    private Duration rangeDuration;
    @RangeChar(min = 'A', max = 'Z')
    private java.lang.Character rangeCharacter;
    @Port
    private int port;
    @Size(min = 1, max = 5)
    private String sizeString;
    @Size(min = 1, max = 5)
    private Object[] sizeArray;
    @Size(min = 1, max = 5)
    private Collection<?> sizeCollection;
    @Size(min = 1, max = 5)
    private Map<?, ?> sizeMap;
    @Matches("[A-Za-z]+")
    private String matches;
    @Hostname
    private String hostname;
    @Email
    private String email;
    @IPv4
    private String ipv4;
    @IPv6
    private String ipv6;
    @Url
    private String url;
    @HexColor
    private String hexColor;
    @Identifier
    private String identifier;
    @Alphabetical
    private String alphabetical;
    @AlphabeticalOrDigit
    private String alphabeticalOrDigit;
    @NotBlank
    private String notBlank;
    @NotEmpty
    private String notEmpty;
    @Port
    @Range(min = 1, max = 100)
    private int minPort;
    @Uuid
    private String uuid;
    @Character
    private String character;
    @After
    private Date afterDate;
    @After
    private Calendar afterCalendar;
    @After
    private TemporalAccessor afterTemporal;
    @AfterOrNow
    private Date afterOrNowDate;
    @AfterOrNow
    private Calendar afterOrNowCalendar;
    @AfterOrNow
    private TemporalAccessor afterOrNowTemporal;
    @Before
    private Date beforeDate;
    @Before
    private Calendar beforeCalendar;
    @Before
    private TemporalAccessor beforeTemporal;
    @BeforeOrNow
    private Date beforeOrNowDate;
    @BeforeOrNow
    private Calendar beforeOrNowCalendar;
    @BeforeOrNow
    private TemporalAccessor beforeOrNowTemporal;

}
