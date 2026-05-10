package it.fulminazzo.blocksmith.checkstyle;

public class DeclarationInvalidOrder {

    String field;

    class Nested { }

    void method() {
        int variable = 1;
        Nested nested = new Nested();
    }

    int method();

    DeclarationInvalidOrder() { }

}