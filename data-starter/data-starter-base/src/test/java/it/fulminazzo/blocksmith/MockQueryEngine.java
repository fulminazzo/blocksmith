package it.fulminazzo.blocksmith;

import it.fulminazzo.blocksmith.data.QueryEngine;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock {@link QueryEngine} for testing purposes.
 */
public class MockQueryEngine implements QueryEngine<Cat, String> {
    @Getter
    private final @NotNull Map<String, Cat> map = new ConcurrentHashMap<>();

}
