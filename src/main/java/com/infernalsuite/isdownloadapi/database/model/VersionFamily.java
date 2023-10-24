package com.infernalsuite.isdownloadapi.database.model;

import com.infernalsuite.isdownloadapi.util.BringChaosToOrder;
import com.infernalsuite.isdownloadapi.util.NameSource;
import com.infernalsuite.isdownloadapi.util.TimeSource;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Comparator;

@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "version_groups")
public record VersionFamily(
        @Id ObjectId _id,
        ObjectId project,
        String name,
        @Nullable Instant time) implements NameSource, TimeSource {

    public static final Comparator<VersionFamily> COMPARATOR = BringChaosToOrder.timeOrNameComparator();
}
