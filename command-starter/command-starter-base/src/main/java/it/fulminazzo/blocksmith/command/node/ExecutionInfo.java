package it.fulminazzo.blocksmith.command.node;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Value
public class ExecutionInfo {
    @NotNull Object executor;
    @NotNull Method method;

}
