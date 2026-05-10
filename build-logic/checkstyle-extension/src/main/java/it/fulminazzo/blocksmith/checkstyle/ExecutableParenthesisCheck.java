package it.fulminazzo.blocksmith.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Validates the declaration of an executable (constructor or method).
 * If the executable parameters are <b>not</b> declared <b>on the same line</b>,
 * then the format should be:
 * <pre>{@code
 * <method_name>(
 *     <parameter_1>,
 *     <parameter_2>,
 *     ...
 *     <parameter_n>
 * )
 * }</pre>
 */
public class ExecutableParenthesisCheck extends AbstractCheck {
    private static final @NotNull String BASE_MESSAGE_PATH = ExecutableParenthesisCheck.class.getPackageName() + ".";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF};
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(final @NotNull DetailAST ast) {
        DetailAST leftPar = ast.findFirstToken(TokenTypes.LPAREN);
        DetailAST rightPar = ast.findFirstToken(TokenTypes.RPAREN);
        if (leftPar == null || rightPar == null || leftPar.getLineNo() == rightPar.getLineNo()) return;
        DetailAST parameters = ast.findFirstToken(TokenTypes.PARAMETERS);
        DetailAST parameter = Objects.requireNonNull(
                parameters,
                "Could not find parameters from executable: " + ast
        ).getFirstChild();
        if (parameter == null) log(parameters, BASE_MESSAGE_PATH + "executable.parenthesis.noParameters");
        else {
            if (parameter.getLineNo() == leftPar.getLineNo())
                log(parameter, BASE_MESSAGE_PATH + "executable.parenthesis.parameters");
            while (parameter.getNextSibling() != null) parameter = parameter.getNextSibling();
            if (parameter.getLineNo() == rightPar.getLineNo())
                log(parameter, BASE_MESSAGE_PATH + "executable.parenthesis.parameters");
        }
    }

}
