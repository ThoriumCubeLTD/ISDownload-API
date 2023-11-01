package com.infernalsuite.isdownloadapi.controller.v1.admin;

import com.infernalsuite.isdownloadapi.database.model.*;
import com.infernalsuite.isdownloadapi.database.repository.*;
import org.bson.types.ObjectId;
import org.json.JSONStringer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(value = UploadAdminController.class)
class UploadAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCollection projectCollection;
    @MockBean
    private VersionFamilyCollection versionFamilyCollection;
    @MockBean
    private VersionCollection versionCollection;
    @MockBean
    private BuildCollection buildCollection;
    @MockBean
    private ArtifactCollection artifactCollection;
    @MockBean
    private LatestCollection latestCollection;

    @Test
    void uploadTest() throws Exception {
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

        Map<String, Artifact.Download> downloads1 = Map.of("1", download1);
        Map<String, Artifact.Download> downloads2 = Map.of("2", download2);

        Project project = new Project(projectId, "project", "project");
        VersionFamily versionFamily = new VersionFamily(versionFamilyId, projectId, "1.20", null);
        Version version = new Version(versionId, versionFamilyId, versionFamilyId, "1.20.2", null);
        Build build = new Build(buildId, versionId, versionFamilyId, 123, now, List.of(change), Build.Channel.PR);
        Artifact artifact1 = new Artifact(artifactId1, projectId, versionId, buildId, "artifact1", downloads1);
        Artifact artifact2 = new Artifact(artifactId2, projectId, versionId, buildId, "artifact2", downloads2);
        Latest latest = new Latest(latestId, projectId, versionId, buildId);

//        Mockito.when(projectCollection.save(Mockito.any())).thenReturn(project);
//        Mockito.when(versionFamilyCollection.save(Mockito.any())).thenReturn(versionFamily);
//        Mockito.when(versionCollection.save(Mockito.any())).thenReturn(version);
//        Mockito.when(buildCollection.save(Mockito.any())).thenReturn(build);
//        Mockito.when(artifactCollection.save(Mockito.any())).thenReturn(artifact1).thenReturn(artifact2);
//        Mockito.when(latestCollection.save(Mockito.any())).thenReturn(latest);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/upload")
                .content(new JSONStringer()
                        .object()
                            .key("projectName").value("project")
                            .key("version").value("1.20.2")
                            .key("build").value(1)
                            .key("time").value(now.toString())
                            .key("changes").array()
                            .object()
                                .key("commit").value("1")
                                .key("summary").value("1")
                                .key("message").value("1")
                            .endObject().endArray()
                            .key("artifacts")
                            .object()
                                .key("artifact1")
                                .object()
                                    .key("1")
                                    .object()
                                        .key("name").value("1")
                                        .key("sha256").value("1")
                                    .endObject()
                                .endObject()
                                .key("artifact2")
                                .object()
                                    .key("2")
                                    .object()
                                        .key("name").value("2")
                                        .key("sha256").value("2")
                                    .endObject()
                                .endObject()
                            .endObject()
                            .key("channel").value("pr")
                        .endObject()
                        .toString())
                .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }
}