package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Build;
import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
import com.infernalsuite.isdownloadapi.database.repository.BuildCollection;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionCollection;
import com.infernalsuite.isdownloadapi.database.repository.VersionFamilyCollection;
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
import java.util.Set;

@WebMvcTest(value = VersionFamilyBuildsController.class)
class VersionFamilyBuildsControllerTest {

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

    @Test
    void givenListOfBuildsAndAVersionFamily_whenGetBuildsFromVersionFamily_returnBuilds() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId versionFamilyId = new ObjectId();
        ObjectId versionId1 = new ObjectId();
        ObjectId versionId2 = new ObjectId();
        ObjectId versionId3 = new ObjectId();
        ObjectId buildId1 = new ObjectId();
        ObjectId buildId2 = new ObjectId();
        ObjectId buildId3 = new ObjectId();
        ObjectId buildId4 = new ObjectId();
        ObjectId buildId5 = new ObjectId();
        ObjectId buildId6 = new ObjectId();

        Instant now = Instant.now();

        Project project = new Project(projectId, "project", "project1");
        VersionFamily versionFamily = new VersionFamily(versionFamilyId, projectId, "1.20", null);
        Version version1 = new Version(versionId1, projectId, versionFamilyId, "1.20.1", null);
        Version version2 = new Version(versionId2, projectId, versionFamilyId, "1.20.2", null);
        Version version3 = new Version(versionId3, projectId, versionFamilyId, "1.20.3", null);
        Build.Change change1 = new Build.Change("1", "1", "1");
        Build.Change change2 = new Build.Change("2", "2", "2");
        Build.Change change3 = new Build.Change("3", "3", "3");
        Build build1 = new Build(buildId1, projectId, versionId1, 1, now, List.of(change1), Build.Channel.STABLE);
        Build build2 = new Build(buildId2, projectId, versionId1, 2, now, List.of(change2), Build.Channel.EXPERIMENTAL);
        Build build3 = new Build(buildId3, projectId, versionId2, 3, now, List.of(change3), Build.Channel.STABLE);
        Build build4 = new Build(buildId4, projectId, versionId2, 4, now, List.of(change1), Build.Channel.PR);
        Build build5 = new Build(buildId5, projectId, versionId3, 5, now, List.of(change2), Build.Channel.EXPERIMENTAL);
        Build build6 = new Build(buildId6, projectId, versionId3, 6, now, List.of(change3), Build.Channel.STABLE);

        Set<ObjectId> versionIds = Set.of(version1._id(), version2._id(), version3._id());

        Mockito.when(projectCollection.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(versionFamilyCollection.findByProjectAndName(projectId, "1.20")).thenReturn(Optional.of(versionFamily));
        Mockito.when(versionCollection.findAllByProjectAndGroup(projectId, versionFamilyId)).thenReturn(List.of(version1, version2, version3));
        Mockito.when(buildCollection.findAllByProjectAndVersionIn(projectId, versionIds)).thenReturn(List.of(build1, build2, build3, build4, build5, build6));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/version_group/1.20/builds");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project1")
                .key("version_group").value("1.20")
                .key("versions").array().value("1.20.1").value("1.20.2").value("1.20.3").endArray()
                .key("builds").array().object()
                .key("version").value("1.20.1")
                .key("build").value(1)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("1")
                .key("summary").value("1")
                .key("message").value("1")
                .endObject().endArray()
                .key("channel").value("stable")
                .endObject()
                .object()
                .key("version").value("1.20.1")
                .key("build").value(2)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("2")
                .key("summary").value("2")
                .key("message").value("2")
                .endObject().endArray()
                .key("channel").value("experimental")
                .endObject()
                .object()
                .key("version").value("1.20.2")
                .key("build").value(3)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("3")
                .key("summary").value("3")
                .key("message").value("3")
                .endObject().endArray()
                .key("channel").value("stable")
                .endObject()
                .object()
                .key("version").value("1.20.2")
                .key("build").value(4)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("1")
                .key("summary").value("1")
                .key("message").value("1")
                .endObject().endArray()
                .key("channel").value("pr")
                .endObject()
                .object()
                .key("version").value("1.20.3")
                .key("build").value(5)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("2")
                .key("summary").value("2")
                .key("message").value("2")
                .endObject().endArray()
                .key("channel").value("experimental")
                .endObject()
                .object()
                .key("version").value("1.20.3")
                .key("build").value(6)
                .key("time").value(now)
                .key("changes").array().object()
                .key("commit").value("3")
                .key("summary").value("3")
                .key("message").value("3")
                .endObject().endArray()
                .key("channel").value("stable")
                .endObject()
                .endArray()
                .endObject();

        System.out.println(result.getResponse().getContentAsString());
        System.out.println(expected.toString());

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), false);
    }

}