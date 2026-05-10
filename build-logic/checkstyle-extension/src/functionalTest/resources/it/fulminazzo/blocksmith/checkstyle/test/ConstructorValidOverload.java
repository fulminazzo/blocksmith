package it.fulminazzo.blocksmith.checkstyle;

public class ConstructorValidOverload {

    ConstructorValidOverload() { }

    ConstructorValidOverload(int valid1) { }

    ConstructorValidOverload(String valid1) { }

    ConstructorValidOverload(int valid1, int valid2) { }

    ConstructorValidOverload(int valid1, String valid2) { }

    ConstructorValidOverload(String valid1, int valid2) { }

    ConstructorValidOverload(String valid1, String valid2) { }

}