package it.fulminazzo.blocksmith.checkstyle;

public class MethodValidOverload {

    void method() { }

    void method(int valid1) { }

    void method(String valid1) { }

    void method(int valid1, int valid2) { }

    void method(int valid1, String valid2) { }

    void method(String valid1, int valid2) { }

    void method(String valid1, String valid2) { }

}