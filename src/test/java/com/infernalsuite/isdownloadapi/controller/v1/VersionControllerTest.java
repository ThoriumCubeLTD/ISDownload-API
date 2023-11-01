package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(value = VersionController.class)
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCollection projectCollection;
    @MockBean
    private VersionCollection versionCollection;
    @MockBean
    private BuildCollection buildCollection;

    Instant now = Instant.now();

    Build.Change change1 = new Build.Change("Test Change Commit", "Test Change Summary", "Test Change Message");
    Build.Change change2 = new Build.Change("Test Change Commit", "Test Change Summary", "Test Change Message");

    @Test
    void givenAVersion_whenGetVersion_returnBuildNumbersInVersion() throws Exception {
        ObjectId projectId = new ObjectId();
        ObjectId versionId1 = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId buildId1 = new ObjectId();
        ObjectId buildId2 = new ObjectId();

        Project project = new Project(projectId, "project", "Test Project Description");
        Version version = new Version(versionId1, projectId, versionFamilyId, "1.20.1", null);
        Build build1 = new Build(buildId1, projectId, versionId1, 1, now, List.of(change1), Build.Channel.STABLE);
        Build build2 = new Build(buildId2, projectId, versionId1, 2, now, List.of(change2), Build.Channel.EXPERIMENTAL);

        Mockito.when(projectCollection.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(versionCollection.findByProjectAndName(projectId, "1.20.1")).thenReturn(Optional.of(version));
        Mockito.when(buildCollection.findAllByProjectAndVersion(projectId, versionId1)).thenReturn(List.of(build1, build2));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/versions/1.20.1");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("Test Project Description")
                .key("version").value("1.20.1")
                .key("builds").array().value(1).value(2).endArray()
                .endObject();

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), false);

    }

}