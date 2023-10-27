package com.infernalsuite.isdownloadapi.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@CompoundIndex(def = "{'project': 1, 'version': 1}")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'number': 1}")
@Document(collection = "builds")
public record Build(
        @Id ObjectId _id,
        ObjectId project,
        ObjectId version,
        int number,
        Instant time,
        List<Change> changes,
        @JsonProperty @Nullable Channel channel
        ) {

    public Channel channelOrDefault() {
        return Objects.requireNonNullElse(this.channel(), Channel.STABLE);
    }

    public enum Channel {
        @JsonProperty("stable")
        STABLE,
        @JsonProperty("experimental")
        EXPERIMENTAL,
        @JsonProperty("pr")
        PR;
    }

    @Schema
    public record Change(
            @Schema(name = "commit")
            String commit,
            @Schema(name = "summary")
            String summary,
            @Schema(name = "message")
            String message
    ) {
    }
}
