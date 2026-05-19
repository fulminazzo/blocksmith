package it.fulminazzo.blocksmith.data;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Mock DTO for testing purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car implements Serializable {
    @Serial
    private static final long serialVersionUID = 4847663321658337364L;

    private String brand;

    private double maxSpeed;

    private Fuel fuel;

    /**
     * Mock DTO for testing purposes.
     */
    public enum Fuel {
        DIESEL,
        HYBRID,
        GAS,
        ELECTRIC
    }

}
