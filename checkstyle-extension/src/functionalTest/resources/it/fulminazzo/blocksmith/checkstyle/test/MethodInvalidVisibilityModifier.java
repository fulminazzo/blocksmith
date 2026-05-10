package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidVisibilityModifier {

    %target%void method(String valid1) { }
    %other%void method(String valid2) { }

    %target%void method(String invalid) { }

}