package it.fulminazzo.blocksmith.checkstyle;

public class FieldInvalidInNestedClass {

    public class valid1 { }
    class valid2 { }
    private class valid3 { }

    public class Inner {

        public class valid1 { }
        class valid2 { }

        public class invalid3 { }

    }

    class invalid4 { }

}