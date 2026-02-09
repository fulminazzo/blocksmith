package it.fulminazzo.blocksmith.sql;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class User {

    Long id;

    String name;

    String lastname;

    String email;

    Integer age;

}
