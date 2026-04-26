package it.fulminazzo.blocksmith.minecraft.dto;

import com.google.gson.Gson;
import it.fulminazzo.blocksmith.minecraft.util.UUIDUtils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contains all the necessary skin data.
 */
@Value
public class SkinData {
    private static final @NotNull Gson GSON = new Gson();

    @NotNull UUID uuid;
    @NotNull String name;

    @NotNull Skin skin;
    @Nullable String cape;

    @Nullable String signature;

    /**
     * Converts the skin data to a base64 encoded string.
     *
     * @return the base64 encoded string
     */
    public @NotNull String toBase64() {
        Map<Object, Object> textures = new HashMap<>();
        textures.put("SKIN", skin.toMap());
        if (cape != null) textures.put("CAPE", Map.of("url", cape));
        Map<?, ?> mappings = Map.of(
                "timestamp", System.currentTimeMillis(),
                "profileId", UUIDUtils.undashed(uuid),
                "profileName", name,
                "signatureRequired", signature != null,
                "textures", textures
        );
        String json = GSON.toJson(mappings);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    /**
     * Converts the skin data from a base64 encoded string.
     *
     * @param base64 the base64 encoded string
     * @return the skin data
     */
    public static @NotNull SkinData fromBase64(final @NotNull String base64, final @Nullable String signature) {
        Map<?, ?> mappings = GSON.fromJson(new String(Base64.getDecoder().decode(base64)), Map.class);
        UUID uuid = UUIDUtils.dashed(mappings.get("profileId").toString());
        String name = mappings.get("profileName").toString();
        Map<?, ?> textures = (Map<?, ?>) mappings.get("textures");
        Map<?, ?> skin = (Map<?, ?>) textures.get("SKIN");
        boolean slim = false;
        if (skin.containsKey("metadata")) {
            Map<?, ?> metadata = (Map<?, ?>) skin.get("metadata");
            slim = metadata.containsKey("model") && metadata.get("model").equals("slim");
        }
        Map<?, ?> cape = (Map<?, ?>) textures.get("CAPE");
        return new SkinData(
                uuid,
                name,
                new SkinData.Skin(
                        skin.get("url").toString(),
                        slim
                ),
                cape == null ? null : (String) cape.get("url"),
                signature
        );
    }

    /**
     * Identifies the actual skin.
     */
    @Value
    public static class Skin {
        @NotNull String url;
        boolean slim;

        private @NotNull Map<?, ?> toMap() {
            Map<Object, Object> map = new HashMap<>();
            map.put("url", url);
            if (slim) map.put("metadata", Map.of("model", "slim"));
            return map;
        }

    }

}
