package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
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
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class VersionBuildsController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
    private final ProjectCollection projects;
    private final VersionCollection versions;
    private final BuildCollection builds;

    @Autowired
    private VersionBuildsController(
            final ProjectCollection projects,
            final VersionCollection versions,
            final BuildCollection builds
    ) {
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
    }

    @ApiResponse(
            content = @Content(
                    schema = @Schema(implementation = BuildsResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds")
    @Operation(summary = "Gets all available builds for a project's version.")
    public ResponseEntity<?> builds(
            @Parameter(name = "project", description = "The project identifier.", example = "paper")
            @PathVariable("project")
            @Pattern(regexp = "[a-z]+") //
            final String projectName,
            @Parameter(description = "A version of the project.")
            @PathVariable("version")
            @Pattern(regexp = Version.PATTERN) //
            final String versionName
    ) {
        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        final List<Build> builds = this.builds.findAllByProjectAndVersion(project._id(), version._id());
        return HTTP.cachedOk(BuildsResponse.from(project, version, builds), CACHE);
    }

    @Schema
    private record BuildsResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "paper")
            String project_id,
            @Schema(name = "project_name", example = "Paper")
            String project_name,
            @Schema(name = "version", pattern = Version.PATTERN, example = "1.18")
            String version,
            @Schema(name = "builds")
            List<VersionBuild> builds
    ) {
        static BuildsResponse from(final Project project, final Version version, final List<Build> builds) {
            return new BuildsResponse(
                    project.name(),
                    project.friendlyName(),
                    version.name(),
                    builds.stream().map(build -> new VersionBuild(
                            build.number(),
                            build.time(),
                            build.changes()
                    )).toList()
            );
        }

        @Schema
        public record VersionBuild(
                @Schema(name = "build", pattern = "\\d+", example = "10")
                int build,
                @Schema(name = "time")
                Instant time,
                @Schema(name = "changes")
                List<Build.Change> changes
        ) {
        }
    }
}
