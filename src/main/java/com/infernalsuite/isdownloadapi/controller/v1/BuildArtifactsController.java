package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Artifact;
import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.repository.ArtifactCollection;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.exception.BuildNotFound;
import com.infernalsuite.isdownloadapi.exception.ProjectNotFound;
import com.infernalsuite.isdownloadapi.exception.VersionNotFound;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BuildArtifactsController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;
    private final ArtifactCollection artifacts;

    @Autowired
    private BuildArtifactsController(
            final ProjectCollection projects,
            final VersionCollection versions,
            final BuildCollection builds,
            final ArtifactCollection artifacts
    ) {
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
        this.artifacts = artifacts;
    }

    @ApiResponse(
            content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @Schema(implementation = ArtifactsResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds/{build:\\d+}/artifacts")
    @Operation(summary = "Gets all available artifacts for a build.")
    public ResponseEntity<?> artifacts(
            @Parameter(name = "project", description = "The project identifier.", example = "aspaper")
            @PathVariable("project")
            @Pattern(regexp = "[a-z]+")
            final String projectName,
            @Parameter(description = "A version of the project.")
            @PathVariable("version")
            @Pattern(regexp = Version.PATTERN)
            final String versionName,
            @Parameter(description = "A build of the project.")
            @PathVariable("build")
            final int buildNumber
    ) {
        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        final Build build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);
        final List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(project._id(), version._id(), build._id());
        return HTTP.cachedOk(ArtifactsResponse.from(project, version, build, artifacts), CACHE);
    }

    @Schema
    private record ArtifactsResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "aspaper")
            String project_Id,
            @Schema(name = "project_name", pattern = "[a-z]+", example = "aspaper")
            String project_Name,
            @Schema(name = "version_id", pattern = Version.PATTERN, example = "1.17.1")
            String version,
            @Schema(name = "build_id", example = "1")
            int build,
            @Schema(name = "artifacts")
            List<BuildArtifact> artifacts
    ) {
        static ArtifactsResponse from(final Project project, final Version version, final Build build, final List<Artifact> artifacts) {
            return new ArtifactsResponse(
                    project.name(),
                    project.friendlyName(),
                    version.name(),
                    build.number(),
                    artifacts.stream().map(artifact -> new BuildArtifact(
                            artifact.name(),
                            artifact.downloads()
                    )).toList());
        }
    }

    @Schema
    public record BuildArtifact(
            @Schema(name = "artifact_name", example = "aspaper-1.17.1-1")
            String name,
            @Schema(name = "downloads")
            Map<String, Artifact.Download> downloads
    ) {
    }
}
