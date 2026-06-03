package it.fulminazzo.blocksmith.checkstyle.validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains the data of a {@link DetailAST} representing an executable declaration in Java (method or constructor).
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Executable implements Comparable<Executable> {
    private static final @NotNull String CONSTRUCTOR_NAME = "<init>";

    @NotNull String name;
    @NotNull List<Parameter> parameters;

    @Override
    public int compareTo(final @NotNull Executable executable) {
        if (!name.equals(executable.name)) return 0;
        List<Parameter> otherParameters = executable.parameters;
        if (parameters.size() < otherParameters.size()) return -1;
        else if (parameters.size() > otherParameters.size()) return 1;
        else for (int i = 0; i < parameters.size(); i++) {
                int compare = parameters.get(i).compareTo(otherParameters.get(i));
                if (compare != 0) return compare;
            }
        return 0;
    }

    /**
     * Creates a new Executable from a {@link DetailAST}.
     *
     * @param executableNode the executable node
     * @return the parameter
     */
    public static @NotNull Executable of(final @NotNull DetailAST executableNode) {
        final String name = executableNode.getType() == TokenTypes.CTOR_DEF
                ? CONSTRUCTOR_NAME
                : Objects.requireNonNull(
                executableNode.findFirstToken(TokenTypes.IDENT),
                "Could not find name node from executable node: " + executableNode
        ).getText();

        DetailAST parameters = Objects.requireNonNull(
                executableNode.findFirstToken(TokenTypes.PARAMETERS),
                "Could not find parameters node from executable node: " + executableNode
        );
        List<Parameter> parametersList = new ArrayList<>();
        for (DetailAST param = parameters.getFirstChild(); param != null; param = param.getNextSibling()) {
            if (param.getType() != TokenTypes.PARAMETER_DEF) continue;
            Parameter parameter = Parameter.of(param);
            parametersList.add(parameter);
        }

        return new Executable(name, parametersList);
    }

    /**
     * Identifies the parameters of an executable.
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Parameter implements Comparable<Parameter> {
        private static final @NotNull List<Integer> PRIMITIVE_TYPES = List.of(
                TokenTypes.LITERAL_BYTE, TokenTypes.LITERAL_SHORT, TokenTypes.LITERAL_CHAR,
                TokenTypes.LITERAL_INT, TokenTypes.LITERAL_LONG,
                TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_DOUBLE,
                TokenTypes.LITERAL_BOOLEAN
        );

        @NotNull String type;
        @NotNull String name;
        @Getter
        boolean primitive;

        @Override
        public int compareTo(final @NotNull Executable.Parameter parameter) {
            if (isPrimitive()) {
                if (parameter.isPrimitive()) return 0;
                else return -1;
            } else {
                if (parameter.isPrimitive()) return 1;
                else return 0;
            }
        }

        /**
         * Creates a new Parameter from a {@link DetailAST}.
         *
         * @param parameterNode the parameter node
         * @return the parameter
         */
        public static @NotNull Parameter of(final @NotNull DetailAST parameterNode) {
            DetailAST name = Objects.requireNonNull(
                    parameterNode.findFirstToken(TokenTypes.IDENT),
                    "Could not find name node from parameter node: " + parameterNode
            );

            DetailAST type = Objects.requireNonNull(
                    parameterNode.findFirstToken(TokenTypes.TYPE),
                    "Could not find type node from parameter node: " + parameterNode
            );
            DetailAST actualType = Objects.requireNonNull(
                    type.getFirstChild(),
                    "Could not find actual type node from parameter node: " + parameterNode
            );

            boolean primitive = PRIMITIVE_TYPES.contains(actualType.getType());
            return new Parameter(actualType.getText(), name.getText(), primitive);
        }
    }

}
