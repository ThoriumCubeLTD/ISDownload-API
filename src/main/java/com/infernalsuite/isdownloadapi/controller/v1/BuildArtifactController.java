package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Artifact;
import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.repository.ArtifactCollection;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.exception.ArtifactNotFound;
import com.infernalsuite.isdownloadapi.exception.BuildNotFound;
import com.infernalsuite.isdownloadapi.exception.ProjectNotFound;
import com.infernalsuite.isdownloadapi.exception.VersionNotFound;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import java.util.Map;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class BuildArtifactController {
    private static CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;
    private final ArtifactCollection artifacts;

    @Autowired
    private BuildArtifactController(
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
            content = @Content(
                    schema = @Schema(implementation = ArtifactResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds/{build:\\d+}/artifacts/{artifact:[a-z0-9\\-]+}")
    @Operation(summary = "Gets a specific artifact for a build.")
    public ResponseEntity<?> artifact(
            @Parameter(name = "project", description = "The project name.", example = "aspaper")
            @PathVariable("project")
            final String projectName,
            @Parameter(name = "version", description = "The version number.", example = "1.20.2")
            @PathVariable("version")
            @Pattern(regexp = Version.PATTERN)
            final String versionName,
            @Parameter(name = "build", description = "The build number.", example = "10")
            @PathVariable("build")
            final int buildNumber,
            @Parameter(name = "artifact", description = "The artifact name.", example = "aspaper-1.0.0")
            @PathVariable("artifact")
            final String artifactName
    ) {
        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        final Build build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);
        final Artifact artifact = this.artifacts.findByProjectAndVersionAndBuildAndName(project._id(), version._id(), build._id(), artifactName).orElseThrow(ArtifactNotFound::new);
        return HTTP.cachedOk(ArtifactResponse.from(project, version, build, artifact), CACHE);
    }

    @Schema
    private record ArtifactResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "aspaper")
            String project_id,
            @Schema(name = "project_name", example = "ASPaper")
            String project_name,
            @Schema(name = "version", pattern = Version.PATTERN, example = "1.0.0")
            String version,
            @Schema(name = "build", example = "1")
            int build,
            @Schema(name = "artifact", example = "aspaper-1.0.0")
            String artifact,
            @Schema(name = "downloads")
            Map<String, Artifact.Download> downloads
    ) {
        static ArtifactResponse from(final Project project, final Version version, final Build build, final Artifact artifact) {
            return new ArtifactResponse(
                    project.name(),
                    project.friendlyName(),
                    version.name(),
                    build.number(),
                    artifact.name(),
                    artifact.downloads()
            );
        }
    }
}
