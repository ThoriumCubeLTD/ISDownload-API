package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionFamilyCollection;
import com.infernalsuite.isdownloadapi.exception.ProjectNotFound;
import com.infernalsuite.isdownloadapi.exception.VersionNotFound;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import org.bson.types.ObjectId;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class VersionFamilyBuildsController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
    private final ProjectCollection projects;
    private final VersionFamilyCollection families;
    private final VersionCollection versions;
    private final BuildCollection builds;

    @Autowired
    private VersionFamilyBuildsController(
            final ProjectCollection projects,
            final VersionFamilyCollection families,
            final VersionCollection versions,
            final BuildCollection builds
    ) {
        this.projects = projects;
        this.families = families;
        this.versions = versions;
        this.builds = builds;
    }

    @ApiResponse(
            content = @Content(
                    schema = @Schema(implementation = VersionFamilyBuildsResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/version_group/{family:" + Version.PATTERN + "}/builds")
    @Operation(summary = "Gets all available builds for a project's version group.")
    public ResponseEntity<?> familyBuilds(
            @Parameter(name = "project", description = "The project identifier.", example = "paper")
            @PathVariable("project")
            @Pattern(regexp = "[a-z]+") //
            final String projectName,
            @Parameter(description = "The version group name.")
            @PathVariable("family")
            @Pattern(regexp = Version.PATTERN) //
            final String familyName
    ) {
        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final VersionFamily family = this.families.findByProjectAndName(project._id(), familyName).orElseThrow(VersionNotFound::new);
        final Map<ObjectId, Version> versions = this.versions.findAllByProjectAndGroup(project._id(), family._id()).stream()
                .collect(Collectors.toMap(Version::_id, Function.identity()));
        final List<Build> builds = this.builds.findAllByProjectAndVersionIn(project._id(), versions.keySet());
        return HTTP.cachedOk(VersionFamilyBuildsResponse.from(project, family, versions, builds), CACHE);
    }

    @Schema
    private record VersionFamilyBuildsResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "aspaper")
            String project_id,
            @Schema(name = "project_name", example = "Paper")
            String project_name,
            @Schema(name = "version_group", pattern = Version.PATTERN, example = "1.18")
            String version_group,
            @Schema(name = "versions")
            List<String> versions,
            @Schema(name = "builds")
            List<VersionFamilyBuild> builds
    ) {
        static VersionFamilyBuildsResponse from(final Project project, final VersionFamily family, final Map<ObjectId, Version> versions, final List<Build> builds) {
            return new VersionFamilyBuildsResponse(
                    project.name(),
                    project.friendlyName(),
                    family.name(),
                    versions.values().stream().sorted(Version.COMPARATOR).map(Version::name).toList(),
                    builds.stream().map(build -> new VersionFamilyBuild(
                            versions.get(build.version()).name(),
                            build.number(),
                            build.time(),
                            build.changes(),
                            build.channelOrDefault()
                    )).toList()
            );
        }

        @Schema
        public static record VersionFamilyBuild(
                @Schema(name = "version", pattern = Version.PATTERN, example = "1.18")
                String version,
                @Schema(name = "build", pattern = "\\d+", example = "10")
                int build,
                @Schema(name = "time")
                Instant time,
                @Schema(name = "changes")
                List<Build.Change> changes,
                @Schema(name = "channel")
                Build.Channel channel
        ) {
        }
    }
}
