package it.fulminazzo.blocksmith.checkstyle;

public class ConstructorInvalidOverload {

    ConstructorInvalidOverload() { }

    ConstructorInvalidOverload(String valid1) { }

    ConstructorInvalidOverload(int invalid1) { }

    ConstructorInvalidOverload(int valid1, int valid2) { }

    ConstructorInvalidOverload(String valid1, String valid2) { }

    ConstructorInvalidOverload(int invalid1, String valid2) { }

    ConstructorInvalidOverload(String valid1, int invalid2) { }

    ConstructorInvalidOverload(int invalid1, int invalid2) { }

}