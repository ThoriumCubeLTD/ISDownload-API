package com.infernalsuite.isdownloadapi.database.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "latest")
public record Latest(
        @Id
        ObjectId id,
        ObjectId project,
        ObjectId version,
        ObjectId build
) {
}
