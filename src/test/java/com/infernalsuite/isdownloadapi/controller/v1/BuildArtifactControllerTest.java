package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Artifact;
import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.repository.ArtifactCollection;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
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

@WebMvcTest(value = BuildArtifactController.class)
class BuildArtifactControllerTest {

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

    @Test
    void givenArtifactAnd_whenGetArtifact_returnArtifact() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId versionId = new ObjectId();
        ObjectId buildId = new ObjectId();
        ObjectId artifactId = new ObjectId();

        Instant now = Instant.now();

        Build.Change change = new Build.Change("1", "1", "1");
        Artifact.Download download1 = new Artifact.Download("1", "1");
        Artifact.Download download2 = new Artifact.Download("2", "2");
        Map<String, Artifact.Download> downloads = Map.of("1", download1, "2", download2);

        Project project = new Project(projectId, "project", "project");
        Version version = new Version(versionId, projectId, versionFamilyId, "1.20.2", null);
        Build build = new Build(buildId, projectId, versionId, 1, now, List.of(change), Build.Channel.STABLE);
        Artifact artifact = new Artifact(artifactId, projectId, versionId, buildId, "artifact", downloads);

        Mockito.when(this.projectCollection.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(this.versionCollection.findByProjectAndName(projectId, "1.20.2")).thenReturn(Optional.of(version));
        Mockito.when(this.buildCollection.findByProjectAndVersionAndNumber(projectId, versionId, 1)).thenReturn(Optional.of(build));
        Mockito.when(this.artifactCollection.findByProjectAndVersionAndBuildAndName(projectId, versionId, buildId, "artifact")).thenReturn(Optional.of(artifact));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/versions/1.20.2/builds/1/artifacts/artifact");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project")
                .key("version").value("1.20.2")
                .key("build").value(1)
                .key("artifact").value("artifact")
                .key("downloads")
                .object()
                .key("2")
                .object()
                .key("name").value("2")
                .key("sha256").value("2")
                .endObject()
                .key("1")
                .object()
                .key("name").value("1")
                .key("sha256").value("1")
                .endObject()
                .endObject()
                .endObject();

        System.out.println(result.getResponse().getContentAsString());
        System.out.println(expected.toString());

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), true);

    }
}