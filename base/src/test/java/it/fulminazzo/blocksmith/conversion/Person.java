package it.fulminazzo.blocksmith.conversion;

import org.jetbrains.annotations.NotNull;

record Person(@NotNull String name, int age) implements Convertible {

}
