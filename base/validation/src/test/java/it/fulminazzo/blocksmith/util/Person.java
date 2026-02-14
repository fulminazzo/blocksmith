package it.fulminazzo.blocksmith.util;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Person {

    @NotEmpty(message = "Name cannot be empty")
    String name;

    @NotEmpty(message = "Lastname cannot be empty")
    String lastname;

    @Min(value = 18, message = "Must be at least 18 years old to use the application")
    int age;

}
