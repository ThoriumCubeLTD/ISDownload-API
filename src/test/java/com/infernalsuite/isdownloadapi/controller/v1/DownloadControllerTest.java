package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import org.apache.coyote.Request;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(DownloadController.class)
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCollection projectCollection;
    @MockBean
    private VersionCollection versionCollection;
    @MockBean
    private BuildCollection buildCollection;
    @MockBean
    private ArtifactCollection artifactCollection;
    @MockBean
    private LatestCollection latestCollection;

    @Test
    void download() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId versionId = new ObjectId();
        ObjectId buildId = new ObjectId();
        ObjectId artifactId = new ObjectId();
        ObjectId latestId = new ObjectId();
        Build.Change change = new Build.Change("1", "1", "1");
        Artifact.Download download1 = new Artifact.Download("1", "1");

        Instant now = Instant.now();

        Project project = new Project(projectId, "project", "project");
        Version version = new Version(versionId, versionFamilyId, versionFamilyId, "1.20.2", null);
        Build build = new Build(buildId, projectId, versionId, 1, now, List.of(change), Build.Channel.STABLE);
        Artifact artifact = new Artifact(artifactId, projectId, versionId, buildId, "artifact", Map.of("1", download1));
        Latest latest = new Latest(latestId, projectId, versionId, buildId);

        Mockito.when(projectCollection.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(versionCollection.findById(versionId)).thenReturn(Optional.of(version));
        Mockito.when(buildCollection.findById(buildId)).thenReturn(Optional.of(build));
        Mockito.when(artifactCollection.findById(artifactId)).thenReturn(Optional.of(artifact));
        Mockito.when(latestCollection.findById(latestId)).thenReturn(Optional.of(latest));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/versions/1.20.2/builds/1/artifacts/artifact/downloads/1");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(200, result.getResponse().getStatus());

    }

    @Test
    void downloadLatest() {
    }
}