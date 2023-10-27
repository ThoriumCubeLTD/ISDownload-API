package com.infernalsuite.isdownloadapi.controller.v1.admin.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CompleteJSONSchema(
        String projectName,
        @Nullable Instant versionFamilyTime,
        String version,
        @Nullable Instant versionTime,
        int buildNumber,
        Instant buildTime,
        List<Map<String, String>> buildChanges,
        Map<String, Map<String, Map<String, String>>> artifacts,
        @Nullable String channel
) {

    public static String toVersionFamily(String version) {
        return version.substring(0, version.lastIndexOf('.'));
    }

}
