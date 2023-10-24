package com.infernalsuite.isdownloadapi.database.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@CompoundIndex(def = "{'project': 1, 'version': 1}")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'number': 1}")
@Document(collection = "builds")
public record Build(
        @Id ObjectId _id,
        ObjectId project,
        ObjectId version,
        int number,
        Instant time,
        List<Change> changes
        ) {

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
