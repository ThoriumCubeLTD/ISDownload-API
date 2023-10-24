package com.infernalsuite.isdownloadapi.util;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

public final class HTTP {
    public static final String APPLICATION_JAVA_ARCHIVE_VALUE = "application/java-archive";
    public static final MediaType APPLICATION_JAVA_ARCHIVE = new MediaType("application", "java-archive");

    private HTTP() {
    }

    public static <T> ResponseEntity<T> cachedOk(final T response, final CacheControl cache) {
        return ResponseEntity.ok().cacheControl(cache).body(response);
    }

    public static CacheControl sMaxAgePublicCache(final Duration sMaxAge) {
        return CacheControl.empty()
                .cachePublic()
                .sMaxAge(sMaxAge);
    }

    public static ContentDisposition attachmentDisposition(final Path filename) {
        return ContentDisposition.attachment().filename(filename.getFileName().toString(), StandardCharsets.UTF_8).build();
    }
}
