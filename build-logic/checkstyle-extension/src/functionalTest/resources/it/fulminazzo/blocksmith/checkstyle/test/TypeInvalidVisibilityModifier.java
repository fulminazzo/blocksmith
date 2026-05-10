package it.fulminazzo.blocksmith.checkstyle;

public class TypeInvalidVisibilityModifier {

    %target%class Valid1 { }
    %other%class Valid2 { }

    %target%class Invalid { }

}