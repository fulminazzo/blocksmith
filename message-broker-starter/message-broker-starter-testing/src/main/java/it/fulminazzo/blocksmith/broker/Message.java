package it.fulminazzo.blocksmith.broker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Identifies a general message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    Long id;

    long time;

    String content;

}
