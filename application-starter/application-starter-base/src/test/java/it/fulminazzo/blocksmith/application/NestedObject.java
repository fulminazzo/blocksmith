package it.fulminazzo.blocksmith.application;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Builder
record NestedObject(
        @NotNull String name,
        @NotNull SimpleObject simple,
        @NotNull Map<String, String> map
) {

}
