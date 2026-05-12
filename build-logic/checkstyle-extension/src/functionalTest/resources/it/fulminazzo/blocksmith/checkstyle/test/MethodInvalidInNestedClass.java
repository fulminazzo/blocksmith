package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidInNestedClass {

    void method() { }

    void method(String valid1) { }

    public class Inner {

        void inner(int valid1, int valid2) { }

        void inner(String valid1, String valid2) { }

        void inner(int invalid1, String valid2) { }

    }

    void method(int invalid1) { }

}