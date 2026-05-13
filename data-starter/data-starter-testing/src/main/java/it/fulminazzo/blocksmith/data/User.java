package it.fulminazzo.blocksmith.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mock DTO for testing purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    Long id;

    String username;

    Integer age;

}
