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

@WebMvcTest(value = VersionBuildController.class)
class VersionBuildControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCollection projectCollection;
    @MockBean
    private VersionCollection versionCollection;
    @MockBean
    private BuildCollection buildCollection;

    @Test
    void givenBuildsAndAVersion_whenGetBuildFromVersion_returnBuild() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId versionId = new ObjectId();
        ObjectId buildId = new ObjectId();

        Instant now = Instant.now();
        Build.Change change = new Build.Change("1", "1", "1");

        Project project = new Project(projectId, "project","project");
        Version version = new Version(versionId, projectId, versionFamilyId, "1.20.2", null);
        Build build = new Build(buildId, projectId, versionId, 1, now, List.of(change), Build.Channel.STABLE);

        Mockito.when(projectCollection.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(versionCollection.findByProjectAndName(projectId, "1.20.2")).thenReturn(Optional.of(version));
        Mockito.when(buildCollection.findByProjectAndVersionAndNumber(projectId, versionId, 1)).thenReturn(Optional.of(build));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/versions/1.20.2/builds/1");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        JSONStringer expected = new JSONStringer();

        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project")
                .key("version").value("1.20.2")
                .key("build").value(1)
                .key("time").value(now)
                .key("changes").array()
                    .object()
                        .key("commit").value("1")
                        .key("summary").value("1")
                        .key("message").value("1")
                    .endObject()
                .endArray()
                .key("channel").value("stable")
                .endObject();

        System.out.println(result.getResponse().getContentAsString());
        System.out.println(expected.toString());

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), false);
    }

}