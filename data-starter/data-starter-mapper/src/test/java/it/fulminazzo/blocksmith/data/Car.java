package it.fulminazzo.blocksmith.data;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car implements Serializable {

    private String brand;

    private double maxSpeed;

    private Fuel fuel;

    public enum Fuel {
        DIESEL,
        HYBRID,
        GAS,
        ELECTRIC
    }

}
