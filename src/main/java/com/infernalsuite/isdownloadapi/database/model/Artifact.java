package com.infernalsuite.isdownloadapi.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Objects;

@CompoundIndex(def = "{'project': 1, 'version': 1")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'build': 1")
@Document(collection = "artifacts")
public record Artifact(
        @Id Object id,
        ObjectId project,
        ObjectId version,
        ObjectId build,
        String name,
        Map<String, Download> downloads,
        @JsonProperty
        @Nullable Channel channel

        ) {

    public Channel channelOrDefault() {
        return Objects.requireNonNullElse(this.channel(), Channel.DEFAULT);
    }

    public enum Channel {
        @JsonProperty("default")
        DEFAULT,
        @JsonProperty("experimental")
        EXPERIMENTAL;
    }

    @Schema
    public record Download(
            @Schema(name = "name", pattern = "[a-z0-9._-]+", example = "")
            String name,
            @Schema(name = "sha256", pattern = "[a-f0-9]{64}", example = "f065e2d345d9d772d5cf2a1ce5c495c4cc56eb2fcd6820e82856485fa19414c8")
            String sha256
    ) {
        // NOTE: this pattern cannot contain any capturing groups
        @Language("RegExp")
        public static final String PATTERN = "[a-zA-Z0-9._-]+";
    }
}
