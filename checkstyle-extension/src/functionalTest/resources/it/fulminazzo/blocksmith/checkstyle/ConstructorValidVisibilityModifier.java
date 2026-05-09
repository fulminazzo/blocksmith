package it.fulminazzo.blocksmith.checkstyle;

public class ConstructorValidVisibilityModifier {

    %target%ConstructorInvalidVisibilityModifier(String valid1) { }
    %target%ConstructorInvalidVisibilityModifier(String valid2) { }

    %other%ConstructorInvalidVisibilityModifier(String valid3) { }
    %other%ConstructorInvalidVisibilityModifier(String valid4) { }

}