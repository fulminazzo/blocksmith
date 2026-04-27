package it.fulminazzo.blocksmith.config.jackson;

import it.fulminazzo.blocksmith.validation.annotation.Max;
import it.fulminazzo.blocksmith.validation.annotation.Min;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Person {

    @NotBlank(exceptionMessage = "name cannot be empty")
    @NonNull(exceptionMessage = "name cannot be null")
    String name = "Alex";

    @NotBlank(exceptionMessage = "lastname cannot be empty")
    @NonNull(exceptionMessage = "lastname cannot be null")
    String lastname = "Fulminazzo";

    @Min(value = 18, exceptionMessage = "minimum age must be 18 years")
    @Max(value = 110, exceptionMessage = "maximum age must be 110 years")
    int age = 23;

    @Min(value = 0, exceptionMessage = "income cannot be negative")
    double income = 0.0;

}
