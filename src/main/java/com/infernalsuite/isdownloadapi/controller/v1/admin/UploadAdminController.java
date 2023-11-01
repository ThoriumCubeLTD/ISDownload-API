package com.infernalsuite.isdownloadapi.controller.v1.admin;

import com.infernalsuite.isdownloadapi.controller.v1.admin.model.CompleteJSONSchema;
import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import com.infernalsuite.isdownloadapi.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bson.types.ObjectId;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadAdminController {

    private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
    private ProjectCollection projects;
    private VersionFamilyCollection versionFamilies;
    private VersionCollection versions;
    private BuildCollection builds;
    private ArtifactCollection artifacts;
    private LatestCollection latest;

   @Autowired
    public UploadAdminController(ProjectCollection projects,
                                 VersionFamilyCollection versionFamilies,
                                 VersionCollection versions,
                                 BuildCollection builds,
                                 ArtifactCollection artifacts,
                                 LatestCollection latest) {
        this.projects = projects;
        this.versionFamilies = versionFamilies;
        this.versions = versions;
        this.builds = builds;
        this.artifacts = artifacts;
        this.latest = latest;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Build added."),
            @ApiResponse(responseCode = "409", description = "Conflicting build already exists.")
    })
    @PostMapping(value = "/v1/admin/upload",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Uploads a new artifact.")
    public ResponseEntity<?> upload(@RequestBody CompleteJSONSchema completeJSONSchema) {
        Optional<Project> project = this.projects.findByName(completeJSONSchema.projectName());
        if (project.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CACHE).build();
        }
        ObjectId projectId = project.get()._id();
        ObjectId versionFamilyId = new ObjectId();

        try {
            Optional<VersionFamily> savedVersionFamily = this.versionFamilies.findByProjectAndName(projectId, CompleteJSONSchema.toVersionFamily(completeJSONSchema.version()));
            if (savedVersionFamily.isEmpty()) {
                VersionFamily versionFamily;
                if (completeJSONSchema.versionFamilyTime() == null) {
                    versionFamily = new VersionFamily(versionFamilyId, projectId, CompleteJSONSchema.toVersionFamily(completeJSONSchema.version()), null);
                } else {
                    versionFamily = new VersionFamily(versionFamilyId, projectId, CompleteJSONSchema.toVersionFamily(completeJSONSchema.version()), completeJSONSchema.versionFamilyTime());
                }
                this.versionFamilies.save(versionFamily);
            } else {
                versionFamilyId = savedVersionFamily.get()._id();
            }
        } catch (NonTransientDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CACHE).build();
        }

        ObjectId versionId = new ObjectId();
        try {
            Optional<Version> savedVersion = this.versions.findByProjectAndName(projectId, completeJSONSchema.version());
            if (savedVersion.isEmpty()) {
                Version version;
                if (completeJSONSchema.versionTime() == null) {
                    version = new Version(versionId, projectId, versionFamilyId, completeJSONSchema.version(), null);
                } else {
                    version = new Version(versionId, projectId, versionFamilyId, completeJSONSchema.version(), completeJSONSchema.versionTime());
                }
                this.versions.save(version);
            } else {
                versionId = savedVersion.get()._id();
            }
        } catch (NonTransientDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CACHE).build();
        }

        ObjectId buildId = new ObjectId();
        try {
            Optional<Build> savedBuild = this.builds.findByProjectAndVersionAndNumber(projectId, versionId, completeJSONSchema.build());
            if (savedBuild.isEmpty()) {
                List<Build.Change> changes = new LinkedList<>();
                completeJSONSchema.buildChanges().forEach(jsonChange -> {
                    Build.Change change = new Build.Change(jsonChange.get("commit"), jsonChange.get("summary"), jsonChange.get("message"));
                    changes.add(change);
                });
                Build build = new Build(buildId, projectId, versionId, completeJSONSchema.build(), completeJSONSchema.buildTime(), changes, Build.Channel.valueOf(completeJSONSchema.channel()));
                this.builds.save(build);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CACHE).build();
            }
        } catch (NonTransientDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CACHE).build();
        }

        List<Artifact> artifacts = this.artifacts.findAllByProjectAndVersionAndBuild(projectId, versionId, buildId);
        if (artifacts.isEmpty()) {
            try {
                ObjectId finalVersionId = versionId;
                ObjectId finalBuildId = buildId;
                Map<String, Artifact> artifactsToSave = completeJSONSchema.artifacts().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new Artifact(new ObjectId(),
                        projectId, finalVersionId, finalBuildId, entry.getKey(), entry.getValue().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey, downloadEntry -> new Artifact.Download(downloadEntry.getValue().get("name"), downloadEntry.getValue().get("sha256"))
                )))));
                this.artifacts.saveAll(artifactsToSave.values());

                Optional<Latest> latest = this.latest.findByProject(projectId);
                Latest newLatest = new Latest(new ObjectId(), projectId, versionId, buildId);
                if (latest.isEmpty()) {
                    this.latest.updateLatest(null, newLatest);
                } else {
                    this.latest.updateLatest(latest.get().id(), newLatest);
                }
            } catch (NonTransientDataAccessException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).cacheControl(CACHE).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CACHE).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CACHE).build();
    }
}
