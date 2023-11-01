package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import org.bson.types.ObjectId;
import org.json.JSONStringer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
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

@WebMvcTest(value = LatestController.class)
class LatestControllerTest {

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
    void givenProjectId_whenGetLatestVersion_returnLatestVersion() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId versionId = new ObjectId();
        ObjectId buildId = new ObjectId();
        ObjectId artifactId1 = new ObjectId();
        ObjectId artifactId2 = new ObjectId();
        ObjectId latestId = new ObjectId();

        Instant now = Instant.now();

        Build.Change change = new Build.Change("1", "1", "1");
        Artifact.Download download1 = new Artifact.Download("1", "1");
        Artifact.Download download2 = new Artifact.Download("2", "2");
        Artifact.Download download3 = new Artifact.Download("3", "3");
        Artifact.Download download4 = new Artifact.Download("4", "4");

        Project project = new Project(projectId, "project", "project");
        Version version = new Version(versionId, projectId, versionFamilyId, "1.20.2", null);
        Build build = new Build(buildId, projectId, versionId, 1, now, List.of(change), Build.Channel.PR);
        Artifact artifact1 = new Artifact(artifactId1, projectId, versionId, buildId, "artifact1", Map.of("normal", download2, "mirror", download1));
        Artifact artifact2 = new Artifact(artifactId2, projectId, versionId, buildId, "artifact2", Map.of("normal", download4, "mirror", download3));

        Latest latest = new Latest(latestId, projectId, versionId, buildId);

        Mockito.when(projectCollection.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(latestCollection.findByProject(projectId)).thenReturn(Optional.of(latest));
        Mockito.when(versionCollection.findById(latest.version())).thenReturn(Optional.of(version));
        Mockito.when(buildCollection.findById(latest.build())).thenReturn(Optional.of(build));
        Mockito.when(artifactCollection.findAllByProjectAndVersionAndBuild(projectId, versionId, buildId)).thenReturn(List.of(artifact1, artifact2));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/latest");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project")
                .key("version").value("1.20.2")
                .key("build").value(1)
                .key("artifacts").array()
                .object()
                .key("name").value("artifact1")
                .key("downloads")
                .object()
                .key("mirror")
                .object()
                .key("name").value("1")
                .key("sha256").value("1")
                .endObject()
                .key("normal")
                .object()
                .key("name").value("2")
                .key("sha256").value("2")
                .endObject()
                .endObject()
                .endObject()
                .object()
                .key("name").value("artifact2")
                .key("downloads")
                .object()
                .key("mirror")
                .object()
                .key("name").value("3")
                .key("sha256").value("3")
                .endObject()
                .key("normal")
                .object()
                .key("name").value("4")
                .key("sha256").value("4")
                .endObject()
                .endObject()
                .endObject()
                .endArray()
                .endObject();

        System.out.println(result.getResponse().getContentAsString());
        System.out.println(expected.toString());

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), true);
    }

}