package com.infernalsuite.isdownloadapi.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfiguration {
    private URL apiBaseUrl;
    private String apiTitle;
    private String apiVersion;
    private @NotNull Path storagePath;

    @SuppressWarnings("checkstyle:MethodName")
    public URL getApiBaseUrl() {
        return this.apiBaseUrl;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void setApiBaseUrl(final URL apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public String getApiTitle() {
        return this.apiTitle;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void setApiTitle(final String apiTitle) {
        this.apiTitle = apiTitle;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public String getApiVersion() {
        return this.apiVersion;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public Path getStoragePath() {
        return this.storagePath;
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void setStoragePath(final Path storagePath) {
        this.storagePath = storagePath;
    }
}
