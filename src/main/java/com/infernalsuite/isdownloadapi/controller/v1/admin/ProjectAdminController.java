package com.infernalsuite.isdownloadapi.controller.v1.admin;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectAdminController {

    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private final ProjectCollection projects;

    @Autowired
    public ProjectAdminController(ProjectCollection projects) {
        this.projects = projects;
    }

    @ApiResponse(responseCode = "201", description = "Project created.")
    @ApiResponse(responseCode = "409", description = "Conflicting project name.")
    @PostMapping(value = "/v1/admin/projects", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adds a project.")
    public ResponseEntity<?> addProject(@RequestBody Project project) {
        Optional<Project> projectOptional = projects.findByName(project.name());
        if (projectOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CACHE).build();
        }
        try {
            Project savedProject = this.projects.save(project);
            return ResponseEntity.ok().cacheControl(CACHE).body(savedProject);
        } catch (NonTransientDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CACHE).build();
        }
    }
}
