package it.fulminazzo.blocksmith.checkstyle;

public class FieldInvalidInNestedClass {

    public String valid1;
    String valid2;
    private String valid3;

    public class Inner {

        public String valid1;
        String valid2;

        public String invalid3;

    }

    String invalid4;

}