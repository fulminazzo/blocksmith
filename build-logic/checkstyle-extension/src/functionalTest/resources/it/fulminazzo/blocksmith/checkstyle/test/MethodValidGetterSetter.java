package it.fulminazzo.blocksmith.checkstyle;

public class MethodValidGetterSetter {

    String getName() { }

    String getName(boolean prefixed) { }

    void setName(String name) { }

    void setName(String prefix, String name) { }

    int getAge() { }

    void setAge(int age) { }

}