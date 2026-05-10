package it.fulminazzo.blocksmith.checkstyle;

public class MethodValidVisibilityModifier {

    %target%void method(String valid1) { }
    %target%void method(String valid2) { }

    %other%void method(String valid3) { }
    %other%void method(String valid4) { }

}