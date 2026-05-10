package it.fulminazzo.blocksmith.checkstyle;

public class ConstructorInvalidInNestedClass {

    ConstructorInvalidInNestedClass() { }

    ConstructorInvalidInNestedClass(String valid1) { }

    public class Inner {

        Inner(int valid1, int valid2) { }

        Inner(String valid1, String valid2) { }

        Inner(int invalid1, String valid2) { }

    }

    ConstructorInvalidInNestedClass(int invalid1) { }

}