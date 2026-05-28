package it.fulminazzo.blocksmith.broker.kafka;

import it.fulminazzo.blocksmith.broker.AbstractMessageBrokerBuilder;
import it.fulminazzo.blocksmith.util.ThreadUtils;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A builder for {@link KafkaMessageBroker}.
 * <br>
 * Example usage:
 * <pre>{@code
 * KafkaMessageBroker messageBroker = KafkaMessageBroker.builder()
 *         .bootstrapServer("0.0.0.0", 9092)
 *         .bootstrapServer("0.0.0.0", 9093)
 *         .securityProtocol(SecurityProtocol.SSL) // defaults to PLAINTEXT
 *         .clientDnsLookup(ClientDnsLookup.RESOLVE_CANONICAL_BOOTSTRAP_SERVERS_ONLY) // defaults to USE_ALL_DNS_IPS
 *         .reconnectBackoff(100L) // defaults to 50
 *         .reconnectBackoffMax(20_000L) // defaults to 1000
 *         .requestTimeout(15_000L) // defaults to 30000
 *         .build();
 * }</pre>
 *
 * @see KafkaMessageBroker
 */
public class KafkaMessageBrokerBuilder
        extends AbstractMessageBrokerBuilder<KafkaMessageBroker, KafkaMessageBrokerBuilder> {
    static final @NotNull String BOOTSTRAP_SERVERS = "bootstrap.servers";
    static final @NotNull String SECURITY_PROTOCOL = "security.protocol";
    static final @NotNull String CLIENT_DNS_LOOKUP = "client.dns.lookup";
    static final @NotNull String RECONNECT_BACKOFF = "reconnect.backoff.ms";
    static final @NotNull String RECONNECT_BACKOFF_MAX = "reconnect.backoff.max.ms";
    static final @NotNull String REQUEST_TIMEOUT = "request.timeout.ms";

    private final @NotNull Set<String> bootstrapServers = new LinkedHashSet<>();

    private final @NotNull Map<String, Object> properties = new HashMap<>();

    private @NotNull SecurityProtocol securityProtocol = SecurityProtocol.PLAINTEXT;
    private @NotNull ClientDnsLookup clientDnsLookup = ClientDnsLookup.USE_ALL_DNS_IPS;

    private int reconnectBackoff = 50;
    private int reconnectBackoffMax = 1_000;

    private int requestTimeout = 30_000;

    private @Nullable ExecutorService executor;

    /**
     * Adds a bootstrap server to the list of servers to connect to.
     *
     * @param host the host
     * @param port the port
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder bootstrapServer(final @NotNull String host, final int port) {
        bootstrapServers.add(host + ":" + port);
        return this;
    }

    /**
     * Sets the security protocol to use.
     * <br>
     * Default: {@link SecurityProtocol#PLAINTEXT}
     *
     * @param securityProtocol the security protocol
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder securityProtocol(final @NotNull SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
        return this;
    }

    /**
     * Sets the client DNS lookup strategy to use.
     * <br>
     * Default: {@link ClientDnsLookup#USE_ALL_DNS_IPS}
     *
     * @param clientDnsLookup the client DNS lookup
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder clientDnsLookup(final @NotNull ClientDnsLookup clientDnsLookup) {
        this.clientDnsLookup = clientDnsLookup;
        return this;
    }

    /**
     * Sets the retry delay when a connection to the broker is lost.
     * The actual delay increases exponentially based on this value until
     * {@link #reconnectBackoffMax(int)} is reached.
     * <br>
     * Default: {@code 50}
     *
     * @param reconnectBackoff the retry backoff (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder reconnectBackoff(final int reconnectBackoff) {
        this.reconnectBackoff = reconnectBackoff;
        return this;
    }

    /**
     * Sets the maximum retry delay when a connection to the broker is lost.
     * <br>
     * Default: {@code 1000}
     *
     * @param reconnectBackoffMax the maximum retry backoff (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder reconnectBackoffMax(final int reconnectBackoffMax) {
        this.reconnectBackoffMax = reconnectBackoffMax;
        return this;
    }

    /**
     * Sets the time to wait for a request response before considering it failed.
     * <br>
     * Default: {@code 30000}
     *
     * @param requestTimeout the request timeout (in milliseconds)
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder requestTimeout(final int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Sets the executor of the queries.
     *
     * @param executor the executor
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder executor(final @NotNull ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Adds a new general property to the settings.
     * <br>
     * <b>WARNING</b>: using this method instead of dedicated ones is allowed but not recommended.
     * <br>
     * Bad:
     * <pre>{@code
     * KafkaMessageBroker.builder().addProperty("request.timeout.ms", 30_000L);
     * }</pre>
     * Good:
     * <pre>{@code
     * KafkaMessageBroker.builder().requestTimeout(30_000L);
     * }</pre>
     *
     * @param key   the key of the property
     * @param value the value of the property
     * @return this object (for method chaining)
     */
    public @NotNull KafkaMessageBrokerBuilder addProperty(final @NotNull String key, final @Nullable Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Gets the bootstrap servers.
     *
     * @return the bootstrap servers
     */
    public @NotNull String getBootstrapServers() {
        if (bootstrapServers.isEmpty())
            throw new IllegalStateException("Bootstrap servers have not been set yet! "
                    + "Please use bootstrapServer before calling this method");
        else return String.join(",", bootstrapServers);
    }

    @Override
    public @NotNull KafkaMessageBroker build() {
        final ExecutorService actualExecutor = Objects.requireNonNullElseGet(
                executor,
                () -> Executors.newCachedThreadPool(
                        ThreadUtils.ownedThreadFactory(KafkaMessageQueryEngine.class)
                )
        );

        Properties properties = new Properties();
        properties.putAll(this.properties);
        properties.put(BOOTSTRAP_SERVERS, getBootstrapServers());
        properties.put(SECURITY_PROTOCOL, this.securityProtocol.name());
        properties.put(CLIENT_DNS_LOOKUP, this.clientDnsLookup.name().toLowerCase(Locale.ROOT));
        properties.put(RECONNECT_BACKOFF, this.reconnectBackoff);
        properties.put(RECONNECT_BACKOFF_MAX, this.reconnectBackoffMax);
        properties.put(REQUEST_TIMEOUT, this.requestTimeout);

        return new KafkaMessageBroker(actualExecutor, properties, getMapper());
    }

}
