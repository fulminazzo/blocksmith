package it.fulminazzo.blocksmith;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Cat {

    @NotEmpty(message = "Cat name should not be empty")
    String name;

    @Min(value = 1, message = "Cat age should at least be 1")
    int age;

    boolean hasOwner;

}
