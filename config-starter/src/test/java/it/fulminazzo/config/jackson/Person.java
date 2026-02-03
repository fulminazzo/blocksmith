package it.fulminazzo.config.jackson;

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

    String name = "Alex";

    String lastname = "Fulminazzo";

    int age = 23;

    double income = 0.0;

}
