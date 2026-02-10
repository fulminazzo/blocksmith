package it.fulminazzo.blocksmith.config.jackson;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "name cannot be null or empty")
    String name = "Alex";

    @NotBlank(message = "lastname cannot be null or empty")
    String lastname = "Fulminazzo";

    @Min(value = 18, message = "minimum age must be 18 years")
    @Max(value = 110, message = "maximum age must be 110 years")
    int age = 23;

    @Min(value = 0, message = "income cannot be negative")
    double income = 0.0;

}
