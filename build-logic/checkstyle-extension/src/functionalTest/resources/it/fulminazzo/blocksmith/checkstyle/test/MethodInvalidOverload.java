package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidOverload {

    void method() { }

    void method(String valid1) { }

    void method(int invalid1) { }

    void method(int valid1, int valid2) { }

    void method(String valid1, String valid2) { }

    void method(int invalid1, String valid2) { }

    void method(String valid1, int invalid2) { }

    void method(int invalid1, int invalid2) { }

}