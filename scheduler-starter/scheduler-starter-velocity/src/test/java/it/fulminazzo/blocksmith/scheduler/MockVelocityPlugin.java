package it.fulminazzo.blocksmith.scheduler;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@RequiredArgsConstructor
public final class MockVelocityPlugin {
    private static @NotNull ProxyServer ignore;

    private final @NotNull ProxyServer server;
    private Logger logger;

}
