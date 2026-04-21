package it.fulminazzo.blocksmith.command.argument

import it.fulminazzo.blocksmith.command.argument.dto.Coordinate
import it.fulminazzo.blocksmith.command.argument.dto.Position
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import it.fulminazzo.blocksmith.message.util.LocaleUtils
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.util.Map.Entry
import java.util.concurrent.TimeUnit

class ArgumentParsersTest extends Specification {

    def 'test that parse of parser for #type returns #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        def arg = argument instanceof Number ? new BigDecimal(argument).toPlainString() : argument.toString()

        when:
        def actual = parser.parse(prepareVisitor(arg))

        then:
        actual == expected

        where:
        type          | argument                              || expected
        // BYTE
        byte          | 1                                     || 1
        byte          | -1                                    || -1
        byte          | Byte.MIN_VALUE                        || Byte.MIN_VALUE
        byte          | Byte.MAX_VALUE                        || Byte.MAX_VALUE
        // BYTE WRAPPER
        Byte          | 1                                     || 1
        Byte          | -1                                    || -1
        Byte          | Byte.MIN_VALUE                        || Byte.MIN_VALUE
        Byte          | Byte.MAX_VALUE                        || Byte.MAX_VALUE
        // SHORT
        short         | 1                                     || 1
        short         | -1                                    || -1
        short         | Short.MIN_VALUE                       || Short.MIN_VALUE
        short         | Short.MAX_VALUE                       || Short.MAX_VALUE
        // SHORT WRAPPER
        Short         | 1                                     || 1
        Short         | -1                                    || -1
        Short         | Short.MIN_VALUE                       || Short.MIN_VALUE
        Short         | Short.MAX_VALUE                       || Short.MAX_VALUE
        // INTEGER
        int           | 1                                     || 1
        int           | -1                                    || -1
        int           | Integer.MIN_VALUE                     || Integer.MIN_VALUE
        int           | Integer.MAX_VALUE                     || Integer.MAX_VALUE
        // INTEGER WRAPPER
        Integer       | 1                                     || 1
        Integer       | -1                                    || -1
        Integer       | Integer.MIN_VALUE                     || Integer.MIN_VALUE
        Integer       | Integer.MAX_VALUE                     || Integer.MAX_VALUE
        // LONG
        long          | 1                                     || 1
        long          | -1                                    || -1
        long          | Long.MIN_VALUE                        || Long.MIN_VALUE
        long          | Long.MAX_VALUE                        || Long.MAX_VALUE
        // LONG WRAPPER
        Long          | 1                                     || 1
        Long          | -1                                    || -1
        Long          | Long.MIN_VALUE                        || Long.MIN_VALUE
        Long          | Long.MAX_VALUE                        || Long.MAX_VALUE
        // FLOAT
        float         | 1                                     || 1
        float         | -1                                    || -1
        float         | -Float.MAX_VALUE                      || -Float.MAX_VALUE
        float         | Float.MAX_VALUE                       || Float.MAX_VALUE
        // FLOAT WRAPPER
        Float         | 1                                     || 1
        Float         | -1                                    || -1
        Float         | -Float.MAX_VALUE                      || -Float.MAX_VALUE
        Float         | Float.MAX_VALUE                       || Float.MAX_VALUE
        // DOUBLE
        double        | 1                                     || 1
        double        | -1                                    || -1
        double        | -Double.MAX_VALUE                     || -Double.MAX_VALUE
        double        | Double.MAX_VALUE                      || Double.MAX_VALUE
        // DOUBLE WRAPPER
        Double        | 1                                     || 1
        Double        | -1                                    || -1
        Double        | -Double.MAX_VALUE                     || -Double.MAX_VALUE
        Double        | Double.MAX_VALUE                      || Double.MAX_VALUE
        // BOOLEAN
        boolean       | true                                  || true
        boolean       | false                                 || false
        // BOOLEAN WRAPPER
        Boolean       | true                                  || true
        Boolean       | false                                 || false
        // CHARACTER
        char          | 'a'                                   || 'a' as Character
        // CHARACTER WRAPPER
        Character     | 'a'                                   || 'a' as Character
        // STRING
        String        | 'Hello!'                              || 'Hello!'
        // ENUM
        TimeUnit      | 'nanoseconds'                         || TimeUnit.NANOSECONDS
        TimeUnit      | 'Microseconds'                        || TimeUnit.MICROSECONDS
        TimeUnit      | 'MILLISECONDS'                        || TimeUnit.MILLISECONDS
        TimeUnit      | 'secondS'                             || TimeUnit.SECONDS
        TimeUnit      | 'MiNuTeS'                             || TimeUnit.MINUTES
        TimeUnit      | 'HOURs'                               || TimeUnit.HOURS
        TimeUnit      | 'DayS'                                || TimeUnit.DAYS
        // LOCALE
        Locale        | 'en_us'                               || Locale.US
        Locale        | 'it_it'                               || Locale.ITALY
        // COORDINATE
        Coordinate    | '~'                                   || new Coordinate(0, true)
        Coordinate    | '1'                                   || new Coordinate(1)
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}2"  || new Coordinate(2, true)
        Coordinate    | '-3'                                  || new Coordinate(-3)
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}-4" || new Coordinate(-4, true)
        // POSITION
        Position      | '1 ~2 ~-3'                            || new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )
        // WORLD POSITION
        WorldPosition | 'world 1 ~2 ~-3'                      || new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )
    }

    def 'test that parse of parser for #type throws exception with #expected message with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        def arg = argument instanceof Number ? new BigDecimal(argument).toPlainString() : argument.toString()

        when:
        parser.parse(prepareVisitor(arg))

        then:
        def e = thrown(ArgumentParseException)
        e.message == expected

        where:
        type      | argument       || expected
        // BYTE
        byte      | ''             || 'error.invalid-number'
        byte      | 'a'            || 'error.invalid-number'
        // BYTE WRAPPER
        Byte      | ''             || 'error.invalid-number'
        Byte      | 'a'            || 'error.invalid-number'
        // SHORT
        short     | ''             || 'error.invalid-number'
        short     | 'a'            || 'error.invalid-number'
        // SHORT WRAPPER
        Short     | ''             || 'error.invalid-number'
        Short     | 'a'            || 'error.invalid-number'
        // INTEGER
        int       | ''             || 'error.invalid-number'
        int       | 'a'            || 'error.invalid-number'
        // INTEGER WRAPPER
        Integer   | ''             || 'error.invalid-number'
        Integer   | 'a'            || 'error.invalid-number'
        // LONG
        long      | ''             || 'error.invalid-number'
        long      | 'a'            || 'error.invalid-number'
        // LONG WRAPPER
        Long      | ''             || 'error.invalid-number'
        Long      | 'a'            || 'error.invalid-number'
        // FLOAT
        float     | ''             || 'error.invalid-number'
        float     | 'a'            || 'error.invalid-number'
        // FLOAT WRAPPER
        Float     | ''             || 'error.invalid-number'
        Float     | 'a'            || 'error.invalid-number'
        // DOUBLE
        double    | ''             || 'error.invalid-number'
        double    | 'a'            || 'error.invalid-number'
        // DOUBLE WRAPPER
        Double    | ''             || 'error.invalid-number'
        Double    | 'a'            || 'error.invalid-number'
        // BOOLEAN
        boolean   | ''             || 'error.invalid-boolean'
        boolean   | 'invalid'      || 'error.invalid-boolean'
        // BOOLEAN WRAPPER
        Boolean   | ''             || 'error.invalid-boolean'
        Boolean   | 'invalid'      || 'error.invalid-boolean'
        // CHARACTER
        char      | ''             || 'error.invalid-character'
        char      | 'ab'           || 'error.invalid-character'
        // CHARACTER WRAPPER
        Character | ''             || 'error.invalid-character'
        Character | 'ab'           || 'error.invalid-character'
        // ENUM
        TimeUnit  | 'day'          || 'error.invalid-enum'
        TimeUnit  | 'weeks'        || 'error.invalid-enum'
        TimeUnit  | 'months'       || 'error.invalid-enum'
        // LOCALE
        Locale    | ''             || 'error.invalid-locale'
        Locale    | 'it'           || 'error.invalid-locale'
        Locale    | 'non_existent' || 'error.invalid-locale'
    }

    def 'test that completions of parser for #type return #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        and:
        def arg = argument instanceof Number ? new BigDecimal(argument).toPlainString() : argument.toString()
        def context = prepareVisitor(arg)

        when:
        def actual = parser.getCompletions(context)

        then:
        actual == expected

        where:
        type          | argument                                                || expected
        // BYTE
        byte          | ''                                                      || (0..9).collect { "$it".toString() }
        byte          | 1                                                       || (0..9).collect { "1$it".toString() }
        byte          | -1                                                      || (0..9).collect { "-1$it".toString() }
        byte          | Byte.MIN_VALUE                                          || []
        byte          | Byte.MAX_VALUE                                          || []
        byte          | 'a'                                                     || []
        // BYTE WRAPPER
        Byte          | ''                                                      || (0..9).collect { "$it".toString() }
        Byte          | 1                                                       || (0..9).collect { "1$it".toString() }
        Byte          | -1                                                      || (0..9).collect { "-1$it".toString() }
        Byte          | Byte.MIN_VALUE                                          || []
        Byte          | Byte.MAX_VALUE                                          || []
        Byte          | 'a'                                                     || []
        // SHORT
        short         | ''                                                      || (0..9).collect { "$it".toString() }
        short         | 1                                                       || (0..9).collect { "1$it".toString() }
        short         | -1                                                      || (0..9).collect { "-1$it".toString() }
        short         | Short.MIN_VALUE                                         || []
        short         | Short.MAX_VALUE                                         || []
        short         | 'a'                                                     || []
        // SHORT WRAPPER
        Short         | ''                                                      || (0..9).collect { "$it".toString() }
        Short         | 1                                                       || (0..9).collect { "1$it".toString() }
        Short         | -1                                                      || (0..9).collect { "-1$it".toString() }
        Short         | Short.MIN_VALUE                                         || []
        Short         | Short.MAX_VALUE                                         || []
        Short         | 'a'                                                     || []
        // INTEGER
        int           | ''                                                      || (0..9).collect { "$it".toString() }
        int           | 1                                                       || (0..9).collect { "1$it".toString() }
        int           | -1                                                      || (0..9).collect { "-1$it".toString() }
        int           | Integer.MIN_VALUE                                       || []
        int           | Integer.MAX_VALUE                                       || []
        int           | 'a'                                                     || []
        // INTEGER WRAPPER
        Integer       | ''                                                      || (0..9).collect { "$it".toString() }
        Integer       | 1                                                       || (0..9).collect { "1$it".toString() }
        Integer       | -1                                                      || (0..9).collect { "-1$it".toString() }
        Integer       | Integer.MIN_VALUE                                       || []
        Integer       | Integer.MAX_VALUE                                       || []
        Integer       | 'a'                                                     || []
        // LONG
        long          | ''                                                      || (0..9).collect { "$it".toString() }
        long          | 1                                                       || (0..9).collect { "1$it".toString() }
        long          | -1                                                      || (0..9).collect { "-1$it".toString() }
        long          | Long.MIN_VALUE                                          || []
        long          | Long.MAX_VALUE                                          || []
        long          | 'a'                                                     || []
        // LONG WRAPPER
        Long          | ''                                                      || (0..9).collect { "$it".toString() }
        Long          | 1                                                       || (0..9).collect { "1$it".toString() }
        Long          | -1                                                      || (0..9).collect { "-1$it".toString() }
        Long          | Long.MIN_VALUE                                          || []
        Long          | Long.MAX_VALUE                                          || []
        Long          | 'a'                                                     || []
        // FLOAT
        float         | ''                                                      || (0..9).collect { "$it".toString() }
        float         | 1                                                       || (0..9).collect { "1$it".toString() }
        float         | -1                                                      || (0..9).collect { "-1$it".toString() }
        float         | -Float.MAX_VALUE                                        || []
        float         | Float.MAX_VALUE                                         || []
        float         | 'a'                                                     || []
        // FLOAT WRAPPER
        Float         | ''                                                      || (0..9).collect { "$it".toString() }
        Float         | 1                                                       || (0..9).collect { "1$it".toString() }
        Float         | -1                                                      || (0..9).collect { "-1$it".toString() }
        Float         | -Float.MAX_VALUE                                        || []
        Float         | Float.MAX_VALUE                                         || []
        Float         | 'a'                                                     || []
        // DOUBLE
        double        | ''                                                      || (0..9).collect { "$it".toString() }
        double        | 1                                                       || (0..9).collect { "1$it".toString() }
        double        | -1                                                      || (0..9).collect { "-1$it".toString() }
        double        | -Double.MAX_VALUE                                       || []
        double        | Double.MAX_VALUE                                        || []
        double        | 'a'                                                     || []
        // DOUBLE WRAPPER
        Double        | ''                                                      || (0..9).collect { "$it".toString() }
        Double        | 1                                                       || (0..9).collect { "1$it".toString() }
        Double        | -1                                                      || (0..9).collect { "-1$it".toString() }
        Double        | -Double.MAX_VALUE                                       || []
        Double        | Double.MAX_VALUE                                        || []
        Double        | 'a'                                                     || []
        // BOOLEAN
        boolean       | ''                                                      || ['true', 'false']
        // BOOLEAN WRAPPER
        Boolean       | ''                                                      || ['true', 'false']
        // CHARACTER
        char          | ''                                                      || ('a'..'z') + ('A'..'Z') + ('0'..'9')
        char          | 'a'                                                     || []
        char          | 'ab'                                                    || []
        // CHARACTER WRAPPER
        Character     | ''                                                      || ('a'..'z') + ('A'..'Z') + ('0'..'9')
        Character     | 'a'                                                     || []
        Character     | 'ab'                                                    || []
        // STRING
        String        | ''                                                      || ['<%name%>']
        // ENUM
        TimeUnit      | ''                                                      || TimeUnit.values().collect { it.toString().toLowerCase() }.sort()
        TimeUnit      | 'n'                                                     || TimeUnit.values().collect { it.toString().toLowerCase() }.sort()
        TimeUnit      | 'nanoseconds'                                           || TimeUnit.values().collect { it.toString().toLowerCase() }.sort()
        TimeUnit      | 'NANOSECONDS'                                           || TimeUnit.values().collect { it.toString().toLowerCase() }.sort()
        // LOCALE
        Locale        | ''                                                      || Locale.availableLocales.findAll { !it.language.empty && !it.country.empty }
                .collect { LocaleUtils.toString(it) }.unique()
        Locale        | 'it'                                                    || Locale.availableLocales.findAll { !it.language.empty && !it.country.empty }
                .collect { LocaleUtils.toString(it) }.unique()
        Locale        | 'it_it'                                                 || Locale.availableLocales.findAll { !it.language.empty && !it.country.empty }
                .collect { LocaleUtils.toString(it) }.unique()
        Locale        | 'non_existent'                                          || Locale.availableLocales.findAll { !it.language.empty && !it.country.empty }
                .collect { LocaleUtils.toString(it) }.unique()
        // COORDINATE
        Coordinate    | ''                                                      || [Coordinate.RELATIVE_IDENTIFIER]
        Coordinate    | 1                                                       || (0..9).collect { "1$it".toString() }
        Coordinate    | -1                                                      || (0..9).collect { "-1$it".toString() }
        Coordinate    | -Double.MAX_VALUE                                       || []
        Coordinate    | Double.MAX_VALUE                                        || []
        Coordinate    | 'a'                                                     || []
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}"                     || (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}$it".toString() }
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}1"                    || (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}1$it".toString() }
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}-1"                   || (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}-1$it".toString() }
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}-${Double.MAX_VALUE}" || []
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}${Double.MAX_VALUE}"  || []
        Coordinate    | "${Coordinate.RELATIVE_IDENTIFIER}a"                    || []
        // POSITION
        Position      | ''                                                      || [Coordinate.RELATIVE_IDENTIFIER]
        Position      | '1'                                                     || (0..9).collect { "1$it".toString() }
        Position      | '1 '                                                    || [Coordinate.RELATIVE_IDENTIFIER]
        Position      | '1 ~2'                                                  ||
                (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}2$it".toString() }
        Position      | '1 ~2 '                                                 || [Coordinate.RELATIVE_IDENTIFIER]
        Position      | '1 ~2 ~-3'                                              ||
                (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}-3$it".toString() }
        // WORLD POSITION
        WorldPosition | ''                                                      || ['<world>']
        WorldPosition | 'world'                                                 || ['<world>']
        WorldPosition | 'world '                                                || [Coordinate.RELATIVE_IDENTIFIER]
        WorldPosition | 'world 1'                                               || (0..9).collect { "1$it".toString() }
        WorldPosition | 'world 1 '                                              || [Coordinate.RELATIVE_IDENTIFIER]
        WorldPosition | 'world 1 ~2'                                            ||
                (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}2$it".toString() }
        WorldPosition | 'world 1 ~2 '                                           || [Coordinate.RELATIVE_IDENTIFIER]
        WorldPosition | 'world 1 ~2 ~-3'                                        ||
                (0..9).collect { "${Coordinate.RELATIVE_IDENTIFIER}-3$it".toString() }
    }

    def 'test that of function throws IllegalArgumentException if type was not recognized'() {
        when:
        ArgumentParsers.of(ArgumentParsersTest)

        then:
        def e = thrown(IllegalArgumentException)
        e.message =~ ".*${ArgumentParsersTest.canonicalName}.+" +
                "${ArgumentParsers.canonicalName}#" +
                "${ArgumentParsers.getMethod('register', Class, ArgumentParser).name}" +
                ".*"
    }

    def 'test that type returns correct type'() {
        given:
        Map<Class<?>, ArgumentParser<?>> parsers = ArgumentParsers.parsers

        expect:
        for (Entry<Class<?>, ArgumentParser<?>> e : parsers.entrySet()) {
            assert e.key == ArgumentParsers.type(e.value)
        }
    }

    def 'test that type throws IllegalArgumentException if parser was not recognized'() {
        when:
        ArgumentParsers.type(Mock(ArgumentParser))

        then:
        def e = thrown(IllegalArgumentException)
        e.message =~ "${ArgumentParsers.canonicalName}#" +
                "${ArgumentParsers.getMethod('register', Class, ArgumentParser).name}" +
                ".*"
    }

    private Visitor<?, ? extends Exception> prepareVisitor(final @NotNull String argument) {
        def context = Mock(Visitor)
        context.input >> new CommandInput().addInput(argument)
        return context
    }

}
