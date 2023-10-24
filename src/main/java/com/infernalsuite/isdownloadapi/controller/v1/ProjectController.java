package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionFamilyCollection;
import com.infernalsuite.isdownloadapi.exception.ProjectNotFound;
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
public class ProjectController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final ProjectCollection projects;
    private final VersionFamilyCollection families;
    private final VersionCollection versions;

    @Autowired
    private ProjectController(
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
                    schema = @Schema(implementation = ProjectResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects/{project:[a-z]+}")
    @Operation(summary = "Gets a list of all available versions for a project.")
    public ResponseEntity<?> project(@Parameter(name = "project", description = "The project identifier.", example = "aspaper")
                                     @PathVariable("project")
                                     @Pattern(regexp = "[a-z]+")
                                     final String projectName) {
        final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        final List<VersionFamily> families = this.families.findAllByProject(project._id());
        final List<Version> versions = this.versions.findAllByProject(project._id());
        return HTTP.cachedOk(ProjectResponse.from(project, families, versions), CACHE);
    }

    @Schema
    private record ProjectResponse(
            @Schema(name = "project_id", pattern = "[a-z]+", example = "paper")
            String project_id,
            @Schema(name = "project_name", example = "Paper")
            String project_name,
            @Schema(name = "version_groups")
            List<String> version_groups,
            @Schema(name = "versions")
            List<String> versions
    ) {
        static ProjectResponse from(final Project project, final List<VersionFamily> families, final List<Version> versions) {
            return new ProjectResponse(
                    project.name(),
                    project.friendlyName(),
                    families.stream().sorted(VersionFamily.COMPARATOR).map(VersionFamily::name).toList(),
                    versions.stream().sorted(Version.COMPARATOR).map(Version::name).toList()
            );
        }
    }
}
