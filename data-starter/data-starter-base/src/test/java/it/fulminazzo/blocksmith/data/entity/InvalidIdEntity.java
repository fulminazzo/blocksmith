package it.fulminazzo.blocksmith.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
final class InvalidIdEntity {

    @Id
    private UUID uuid;

    @Id
    private String name;

}
