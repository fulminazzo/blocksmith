package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidName {

    void %target%(String valid1) { }
    void %other%(String valid2) { }

    void %target2%(String invalid) { }

}