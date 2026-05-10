package it.fulminazzo.blocksmith.checkstyle;

public class MethodInvalidOverloadGrouping {

    void method1() { }

    void method2() { }

    void method1(int invalid) { }

    void method3() { }

    void method2(int invalid) { }

}