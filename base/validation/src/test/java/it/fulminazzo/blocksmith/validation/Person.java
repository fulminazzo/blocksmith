package it.fulminazzo.blocksmith.validation;

import it.fulminazzo.blocksmith.validation.annotation.Alphabetical;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.NotEmpty;
import it.fulminazzo.blocksmith.validation.annotation.Range;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
final class Person {

    @Alphabetical
    @NonNull
    String name;

    @Range(min = 18, max = 115)
    final int age;

    final Person.School school;

    void setName(final @Alphabetical @NonNull String name,
                 final @NotEmpty @NonNull String reason) {
        Validator.validateMethod(name, reason);
        this.name = name;
        System.out.println("updated name because: " + reason);
    }

    @Value
    static class School {

        @Alphabetical
        @NonNull
        String name;

    }

}
