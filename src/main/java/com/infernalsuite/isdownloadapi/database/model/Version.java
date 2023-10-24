package com.infernalsuite.isdownloadapi.database.model;

import com.infernalsuite.isdownloadapi.util.BringChaosToOrder;
import com.infernalsuite.isdownloadapi.util.NameSource;
import com.infernalsuite.isdownloadapi.util.TimeSource;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Comparator;

@CompoundIndex(def = "{'project': 1, 'group': 1}")
@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "versions")
public record Version(
        @Id ObjectId _id,
        ObjectId project,
        ObjectId group,
        String name,
        @Nullable Instant time
        ) implements NameSource, TimeSource {
    // NOTE: this pattern cannot contain any capturing groups
    @Language("RegExp")
    public static final String PATTERN = "[0-9.]+-?(?:pre|SNAPSHOT)?(?:[0-9.]+)?";
    public static final Comparator<Version> COMPARATOR = BringChaosToOrder.timeOrNameComparator();
}
