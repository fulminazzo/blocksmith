package it.fulminazzo.blocksmith.checkstyle.test;

public class ExecutableInvalidParenthesis {

    void valid1() { }

    void invalid2(String first,
            int second) { }

    ExecutableInvalidParenthesis() { }

    ExecutableInvalidParenthesis(boolean a,
                                 float b) { }

    void invalid3(

    ) { }

}