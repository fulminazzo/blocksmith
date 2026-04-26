package it.fulminazzo.blocksmith.minecraft.util;

import com.google.gson.Gson;
import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.minecraft.dto.GameProfile;
import it.fulminazzo.blocksmith.minecraft.dto.SkinData;
import it.fulminazzo.blocksmith.structure.expiring.ExpiringMap;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A collection of utilities to work with Mojang's API.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class APIUtils {
    /**
     * The default Blocksmith User-Agent header value.
     */
    public static final String USER_AGENT = String.format("%s/%s", ProjectInfo.PROJECT_NAME, ProjectInfo.VERSION);

    /**
     * The base API URL.
     */
    public static final @NotNull String API_URL = "https://api.mojang.com";
    /**
     * The base session server API URL.
     */
    public static final @NotNull String SESSION_SERVER_API = "https://sessionserver.mojang.com";

    /**
     * The URL to get the (undashed) UUID of a player by its name.
     */
    public static final @NotNull String UUID_BY_NAME_URL = API_URL + "/users/profiles/minecraft/%s";

    /**
     * The URL to get the profile of a player by its (undashed) UUID.
     */
    public static final @NotNull String PROFILE_BY_UNDASHED_UUID_URL = SESSION_SERVER_API + "/session/minecraft/profile/%s";

    private static final @NotNull HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30L))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final @NotNull Gson GSON = new Gson();

    private static final @NotNull ExpiringMap<UUID, GameProfile> PROFILE_CACHE = ExpiringMap.lazy();
    private static final @NotNull Duration PROFILE_CACHE_TTL = Duration.ofMinutes(5L);

    private static final @NotNull Map<String, UUID> NAME_CACHE = new ConcurrentHashMap<>();

    /**
     * Fetches the session server API to get the profile associated with the given player id.
     * <br>
     * If not found or any general error occurs, an empty {@link Optional} is returned.
     *
     * @param uuid the id of the profile
     * @return the profile if found
     */
    public static @NotNull Optional<GameProfile> getUserProfile(final @NotNull UUID uuid) {
        try {
            if (PROFILE_CACHE.containsKey(uuid)) return Optional.of(PROFILE_CACHE.get(uuid));
            HttpRequest request = requestBuilder(String.format(PROFILE_BY_UNDASHED_UUID_URL, uuid.toString().replace("-", ""))).GET().build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> data = GSON.fromJson(response.body(), Map.class);
            if (data != null) {
                SkinData skinData = null;
                if (data.get("properties") != null) {
                    List<?> list = (List<?>) data.get("properties");
                    if (!list.isEmpty()) {
                        Map<?, ?> map = (Map<?, ?>) list.get(0);
                        String value = (String) map.get("value");
                        String signature = (String) map.get("signature");
                        skinData = SkinData.fromBase64(value, signature);
                    }
                }
                GameProfile profile = new GameProfile(uuid, (String) data.get("name"), skinData);
                PROFILE_CACHE.put(uuid, profile, PROFILE_CACHE_TTL);
                return Optional.of(profile);
            }
        } catch (IOException | InterruptedException ignored) {
            // ignored
        }
        return Optional.empty();
    }

    /**
     * Fetches the Mojang's API to get the {@link UUID} associated with the given name.
     * <br>
     * If not found or any general error occurs, an empty {@link Optional} is returned.
     *
     * @param name the name
     * @return the uuid if found
     */
    public static @NotNull Optional<UUID> getUuidFromName(final @NotNull String name) {
        try {
            if (NAME_CACHE.containsKey(name)) return Optional.of(NAME_CACHE.get(name));
            HttpRequest request = requestBuilder(String.format(UUID_BY_NAME_URL, name)).GET().build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> data = GSON.fromJson(response.body(), Map.class);
            if (data == null) return Optional.empty();
            Object id = data.get("id");
            if (id == null) return Optional.empty();
            UUID uuid = UUIDUtils.dashed(id.toString());
            NAME_CACHE.put(name, uuid);
            return Optional.of(uuid);
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }

    /**
     * Starts a {@link HttpRequest} builder with the given URL and the default headers.
     *
     * @param url the url
     * @return the builder
     */
    static @NotNull HttpRequest.Builder requestBuilder(final @NotNull String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Cache-Control", "no-cache, no-store, must-revalidate");
    }

}
