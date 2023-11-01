package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.configuration.AppConfiguration;
import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import com.infernalsuite.isdownloadapi.exception.*;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DownloadController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofHours(12));
    private final AppConfiguration configuration;
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;
    private final ArtifactCollection artifacts;
    private final LatestCollection latest;
    private final Logger logger = LoggerFactory.getLogger(DownloadController.class);


    @Autowired
    private DownloadController(
            final AppConfiguration configuration,
            final ProjectCollection projects,
            final VersionCollection versions,
            final BuildCollection builds,
            final ArtifactCollection artifacts,
            LatestCollection latest) {
        this.configuration = configuration;
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
        this.artifacts = artifacts;
        this.latest = latest;
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

//        Artifact.Download download = artifact.downloads().get(downloadName);
//        if (download == null) {
//            throw new DownloadNotFound();
//        } else {
//            try {
//                return new JavaArchive(
//                        this.configuration.getStoragePath()
//                                .resolve(project.name())
//                                .resolve(version.name())
//                                .resolve(String.valueOf(build.number()))
//                                .resolve(artifact.name())
//                                .resolve(download.name()),
//                        CACHE
//                );
//            } catch (final IOException e) {
//                throw new DownloadFailed(e);
//            }
//        }

        for (final Map.Entry<String, Artifact.Download> download: artifact.downloads().entrySet()) {
            System.out.println(download.getKey() + " " + download.getValue().name());
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
//    @GetMapping(value = "/v1/projects/{project:[a-z]+}/latest/download",
//            produces = {
//                    MediaType.APPLICATION_JSON_VALUE,
//                    MediaType.APPLICATION_OCTET_STREAM_VALUE
//            })
//    @Operation(summary = "Download the given file from the given artifact")
//    public ResponseEntity<?> downloadLatestFromProject(
//            @Parameter(name = "project")
//            @PathVariable("project")
//            @Pattern(regexp = "[a-z]+")
//            final String projectName
//    ) {
//        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
//        final Latest latest = this.latest.findByProject(project._id()).orElseThrow(LatestNotFound::new);
//        final Version version = this.versions.findById(latest.version()).orElseThrow(VersionNotFound::new);
//        final Build build = this.builds.findById(latest.build()).orElseThrow(BuildNotFound::new);
//        final List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(project._id(), version._id(), build._id());
//
//        Path filePath = this.configuration.getStoragePath().resolve(project.name()).resolve(version.name()).resolve(String.valueOf(build.number()));
//
//
//        for (Artifact artifact: artifacts) {
//            for (Map.Entry<String, Artifact.Download> download: artifact.downloads().entrySet()) {
//                Path path = filePath.resolve(artifact.name()).resolve(download.getValue().name());
//            }
//            throw new DownloadNotFound();
//        }
//        throw new DownloadNotFound();
//    }

    @ApiResponse(
            responseCode = "200",
            headers = {
                    @Header(
                            name = "Content-Disposition",
                            description = "A header indicating that the content is expected to be displayed as an attachment, that is downloaded and saved locally.",
                            schema = @Schema(type = "string")
                    ),
                    @Header(
                            name = "Last-Modified",
                            description = "The date and time at which the origin server believes the resource was last modified.",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/latest/download")
    @Operation(summary = "Download the given file from the given artifact")
    public void downloadLatest(
            HttpServletResponse response,
            @Parameter(name = "project")
            @PathVariable("project")
            @Pattern(regexp = "[a-z]+")
            final String projectName) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + projectName + ".zip");
        response.setHeader("Cache-Control", "no-store");
        response.setStatus(HttpServletResponse.SC_OK);

        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Latest latest = this.latest.findByProject(project._id()).orElseThrow(LatestNotFound::new);
        final Version version = this.versions.findById(latest.version()).orElseThrow(VersionNotFound::new);
        final Build build = this.builds.findById(latest.build()).orElseThrow(BuildNotFound::new);
        final List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(project._id(), version._id(), build._id());

        List<String> filePaths = new ArrayList<>();
        for (Artifact artifact: artifacts) {
            for (Map.Entry<String, Artifact.Download> download: artifact.downloads().entrySet()) {
                Path path = this.configuration.getStoragePath().resolve(project.name()).resolve(version.name()).resolve(String.valueOf(build.number())).resolve(artifact.name()).resolve(download.getValue().name());
                filePaths.add(path.toString());
            }
        }

        logger.error("Size = " + filePaths.size());


        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (String filePath: filePaths) {
                logger.error("Adding to zip file " + filePath);
                FileSystemResource resource = new FileSystemResource(filePath);
                if (!resource.exists()) {
                    throw new DownloadNotFound();
                }
                ZipEntry zipEntry = new ZipEntry(resource.getFilename());
                zipEntry.setSize(resource.contentLength());
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);
                StreamUtils.copy(resource.getInputStream(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        } catch (IOException e) {
            throw new DownloadFailed(e);
        }
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

    private static class ZipArchive extends ResponseEntity<FileSystemResource> {
        ZipArchive(final Path path, final CacheControl cache) throws IOException {
            super(new FileSystemResource(path), headersFor(path, cache), HttpStatus.OK);
        }

        private static HttpHeaders headersFor(final Path path, final CacheControl cache) throws IOException {
            final HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(cache);
            headers.setContentDisposition(HTTP.attachmentDisposition(path.getFileName()));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
            return headers;
        }

    }
}
