package it.fulminazzo.blocksmith.broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Identifies a general message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 3291081753698614993L;

    Long id;

    long time;

    String content;

}
