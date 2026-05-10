package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidGetterSetter {

    String getName() { }

    void setName(String name) { }

    String getName(boolean prefixed) { }

    int getAge() { }

    void setName(String prefix, String name) { }

}