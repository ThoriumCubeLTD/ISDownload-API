package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.configuration.AppConfiguration;
import com.infernalsuite.isdownloadapi.database.model.Artifact;
import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.repository.ArtifactCollection;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.exception.*;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DownloadController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final AppConfiguration configuration;
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;
    private final ArtifactCollection artifacts;


    @Autowired
    private DownloadController(
            final AppConfiguration configuration,
            final ProjectCollection projects,
            final VersionCollection versions,
            final BuildCollection builds,
            final ArtifactCollection artifacts
    ) {
        this.configuration = configuration;
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
        this.artifacts = artifacts;
    }

    @ApiResponse(
            responseCode = "200",
            headers = {
                    @Header(
                            name = "Content-Disposition",
                            description = "A header indicating that the content is expected to be displayed as an attachment, that is downloaded and saved locally.",
                            schema = @Schema(type = "string")
                    ),
                    @Header(
                            name = "ETag",
                            description = "An identifier for a specific version of a resource. It lets caches be more efficient and save bandwidth, as a web server does not need to resend a full response if the content has not changed.",
                            schema = @Schema(type = "string")
                    ),
                    @Header(
                            name = "Last-Modified",
                            description = "The date and time at which the origin server believes the resource was last modified.",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @GetMapping(value = "/v1/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds/{build:\\d+}/artifacts/{artifact:[a-z0-9\\-]+}/downloads/{download:" + Artifact.Download.PATTERN + "}",
    produces = {
            MediaType.APPLICATION_JSON_VALUE,
            HTTP.APPLICATION_JAVA_ARCHIVE_VALUE
    })
    @Operation(summary = "Download the given file from the given artifact")
    public ResponseEntity<?> download(
            @Parameter(name = "project", description = "The project name", example = "aspaper")
            @PathVariable("project")
            @Pattern(regexp = "[a-z]+")
            final String projectName,
            @Parameter(name = "version", description = "The version", example = "1.0.0")
            @PathVariable("version")
            @Pattern(regexp = Version.PATTERN)
            final String versionName,
            @Parameter(name = "build", description = "The build number", example = "1")
            @PathVariable("build")
            @Pattern(regexp = "\\d+")
            final int buildNumber,
            @Parameter(name = "artifact", description = "The artifact name", example = "aspaper")
            @PathVariable("artifact")
            @Pattern(regexp = "[a-z0-9\\-]+")
            final String artifactName,
            @Parameter(name = "download", description = "The download name", example = "aspaper.jar")
            @PathVariable("download")
            @Pattern(regexp = Artifact.Download.PATTERN)
            final String downloadName
    ) {
        final Project project = projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Version version = versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        final Build build = builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);
        final Artifact artifact = artifacts.findByProjectAndVersionAndBuildAndName(project._id(), version._id(), build._id(), artifactName).orElseThrow(ArtifactNotFound::new);
        for (final Map.Entry<String, Artifact.Download> download: artifact.downloads().entrySet()) {
            if (download.getValue().name().equals(downloadName)) {
                try {
                    return new JavaArchive(
                            this.configuration.getStoragePath()
                                    .resolve(project.name())
                                    .resolve(version.name())
                                    .resolve(String.valueOf(build.number()))
                                    .resolve(artifact.name())
                                    .resolve(download.getValue().name()),
                            CACHE
                    );
                } catch (final IOException e) {
                    throw new DownloadFailed(e);
                }
            }
        }
        throw new DownloadNotFound();
    }

    private static class JavaArchive extends ResponseEntity<FileSystemResource> {
        JavaArchive(final Path path, final CacheControl cache) throws IOException {
            super(new FileSystemResource(path), headersFor(path, cache), HttpStatus.OK);
        }

        private static HttpHeaders headersFor(final Path path, final CacheControl cache) throws IOException {
            final HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(cache);
            headers.setContentDisposition(HTTP.attachmentDisposition(path.getFileName()));
            headers.setContentType(HTTP.APPLICATION_JAVA_ARCHIVE);
            headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
            return headers;
        }
    }
}
