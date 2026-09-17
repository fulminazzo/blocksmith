package it.fulminazzo.blocksmith.application;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
record SimpleObject(@NotNull String name) {
}
