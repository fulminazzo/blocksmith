package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.validation.annotation.Min;
import it.fulminazzo.blocksmith.validation.annotation.NonNull;
import it.fulminazzo.blocksmith.validation.annotation.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Cat {

    @NonNull(exceptionMessage = "Cat name should not be empty")
    @NotEmpty(exceptionMessage = "Cat name should not be empty")
    String name;

    @Min(value = 1, exceptionMessage = "Cat age should at least be 1")
    int age;

    boolean hasOwner;

}
