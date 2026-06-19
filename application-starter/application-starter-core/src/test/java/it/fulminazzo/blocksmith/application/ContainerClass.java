package it.fulminazzo.blocksmith.application;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

@SuppressWarnings("unused")
@Data
final class ContainerClass implements Application {
    private final FieldClass field = new FieldClass();
    private final MethodClass method = new MethodClass();

    private Object target;

    @Override
    public @NotNull File directory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Logger logger() {
        throw new UnsupportedOperationException();
    }

    @Data
    static final class FieldClass {
        String name = "Alex";

        MethodClass ageData = new MethodClass();

    }

    @Data
    static final class MethodClass {

        int getAge() {
            return 10;
        }

        void setAge(int age) {

        }

        NestedClass getInformation() {
            return new NestedClass();
        }

        String getLocalizedName(final String name) {
            throw new UnsupportedOperationException();
        }

    }

    @Data
    static final class NestedClass {
        String identity = "Batman";

    }

}
