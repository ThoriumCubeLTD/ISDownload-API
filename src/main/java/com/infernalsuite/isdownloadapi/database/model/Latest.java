package com.infernalsuite.isdownloadapi.database.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "latest")
public record Latest(
        ObjectId project,
        ObjectId version,
        ObjectId build
) {
}
