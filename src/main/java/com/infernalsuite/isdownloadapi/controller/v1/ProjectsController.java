package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectsController {
    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final ProjectCollection projects;

    @Autowired
    public ProjectsController(ProjectCollection projects) {
        this.projects = projects;
    }

    @ApiResponse(
            content = @Content(
                    schema = @Schema(implementation = ProjectsResponse.class)
            ),
            responseCode = "200"
    )
    @GetMapping("/v1/projects")
    @Operation(summary = "Gets a list of all available projects.")
    public ResponseEntity<?> projects() {
        final List<Project> projects = this.projects.findAll();
        return HTTP.cachedOk(ProjectsResponse.from(projects), CACHE);
    }

    @Schema
    private record ProjectsResponse(
            @Schema(name = "projects")
            List<String> projects
    ) {
        static ProjectsResponse from(final List<Project> projects) {
            return new ProjectsResponse(projects.stream().map(Project::name).toList());
        }
    }
}
