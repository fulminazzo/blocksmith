package it.fulminazzo.blocksmith.checkstyle;

public class ConstructorInvalidVisibilityModifier {

    %target%ConstructorInvalidVisibilityModifier(String valid1) { }
    %other%ConstructorInvalidVisibilityModifier(String valid2) { }

    %target%ConstructorInvalidVisibilityModifier(String invalid) { }

}