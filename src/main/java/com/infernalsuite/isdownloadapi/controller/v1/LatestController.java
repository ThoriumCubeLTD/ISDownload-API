package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.configuration.AppConfiguration;
import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import com.infernalsuite.isdownloadapi.exception.BuildNotFound;
import com.infernalsuite.isdownloadapi.exception.LatestNotFound;
import com.infernalsuite.isdownloadapi.exception.ProjectNotFound;
import com.infernalsuite.isdownloadapi.exception.VersionNotFound;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LatestController {

    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final LatestCollection latest;
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;
    private final ArtifactCollection artifacts;
    private final Logger logger = LoggerFactory.getLogger(LatestController.class);

    public LatestController(LatestCollection latest, ProjectCollection projects, VersionCollection versions, BuildCollection builds, ArtifactCollection artifacts, AppConfiguration configuration) {
        this.latest = latest;
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
        this.artifacts = artifacts;
    }

    @ApiResponse(
            content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @Schema(implementation = LatestResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/latest")
    @Operation(summary = "Get latest version of the project")
    public ResponseEntity<?> latestFromProject(@Parameter(name = "project")
                                       @PathVariable("project")
                                       @Pattern(regexp = "[a-z]+")
                                       final String projectName) {

        logger.info("projectName: " + projectName);

        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Latest latest = this.latest.findByProject(project._id()).orElseThrow(LatestNotFound::new);
        final Version version = this.versions.findById(latest.version()).orElseThrow(VersionNotFound::new);
        final Build build = this.builds.findById(latest.build()).orElseThrow(BuildNotFound::new);
        final List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(project._id(), version._id(), build._id());
        return HTTP.cachedOk(LatestResponse.from(project, version, build, artifacts), CACHE);
    }

//    @ApiResponse(
//            content = @io.swagger.v3.oas.annotations.media.Content(
//                    schema = @Schema(implementation = LatestResponse.class)
//            ),
//            responseCode = "200"
//    )
//    @GetMapping("/v1/projects/{project:[a-z]+}/version/{version:" + Version.PATTERN + "}/latest")
//    @Operation(summary = "Get latest version of the project")
//    public ResponseEntity<?> latestFromProjectVersion(
//            @Parameter(name = "project")
//            @PathVariable("project")
//            @Pattern(regexp = "[a-z]+")
//            final String projectName,
//            @Parameter(name = "version")
//            @PathVariable("version")
//            @Pattern(regexp = Version.PATTERN)
//            final String versionName) {
//        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
//        final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
//        final Latest latest = this.latest.findByProjectAndVersion(project._id(), version._id()).orElseThrow(LatestNotFound::new);
//        final Build build = this.builds.findById(latest.build()).orElseThrow(BuildNotFound::new);
//        final List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(project._id(), version._id(), build._id());
//        return HTTP.cachedOk(LatestResponse.from(project, version, build, artifacts), CACHE);
//    }



    private record LatestResponse(
            @Schema(name = "project_id", description = "Project ID", example = "aspaper")
            String project_id,
            @Schema(name = "project_name", description = "Project ID", example = "ASPaper")
            String project_name,
            @Schema(name = "version_id", description = "Version ID", example = "1.17.1")
            String version_id,
            @Schema(name = "build_id", description = "Build ID", example = "10")
            int buildNumber,
            @Schema(name = "artifacts")
            List<LatestArtifact> artifacts
    ) {
        static LatestResponse from(final Project project, final Version version, final Build build, final List<Artifact> artifacts) {
            return new LatestResponse(
                    project.name(),
                    project.friendlyName(),
                    version.name(),
                    build.number(),
                    artifacts.stream().map(artifact -> new LatestArtifact(
                            artifact.name(),
                            artifact.downloads()
                    )).toList());
        }
    }

    public record LatestArtifact(
            @Schema(name = "artifact_name", example = "server")
            String id,
            @Schema(name = "downloads")
            Map<String, Artifact.Download> downloads
    ) {
    }
}
