package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
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

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class VersionFamilyController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
    private final ProjectCollection projects;
    private final VersionFamilyCollection families;
    private final VersionCollection versions;

    @Autowired
    private VersionFamilyController(
            final ProjectCollection projects,
            final VersionFamilyCollection families,
            final VersionCollection versions
    ) {
        this.projects = projects;
        this.families = families;
        this.versions = versions;
    }

    @ApiResponse(
            content = @Content(
                    schema = @Schema(implementation = VersionFamilyResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}/version_group/{family:" + Version.PATTERN + "}")
    @Operation(summary = "Gets information about a project's version group.")
    public ResponseEntity<?> family(
            @Parameter(name = "project", description = "The project identifier.", example = "aspaper")
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
        final List<Version> versions = this.versions.findAllByProjectAndGroup(project._id(), family._id());
        return HTTP.cachedOk(VersionFamilyResponse.from(project, family, versions), CACHE);
    }

    @Schema
    private record VersionFamilyResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "paper")
            String project_id,
            @Schema(name = "project_name", example = "Paper")
            String project_name,
            @Schema(name = "version_group", pattern = Version.PATTERN, example = "1.18")
            String version_group,
            @Schema(name = "versions")
            List<String> versions
    ) {
        static VersionFamilyResponse from(final Project project, final VersionFamily family, final List<Version> versions) {
            return new VersionFamilyResponse(
                    project.name(),
                    project.friendlyName(),
                    family.name(),
                    versions.stream().sorted(Version.COMPARATOR).map(Version::name).toList()
            );
        }
    }
}
