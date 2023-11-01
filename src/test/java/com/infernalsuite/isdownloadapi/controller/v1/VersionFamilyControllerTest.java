package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.model.Version;
import com.infernalsuite.isdownloadapi.database.model.VersionFamily;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(value = VersionFamilyController.class)
class VersionFamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectCollection projects;
    @MockBean
    private VersionFamilyCollection families;
    @MockBean
    private VersionCollection versions;

    @Test
    public void givenVersionFamily_whenGetVersionFamily_returnVersions() throws Exception {

        ObjectId projectId = new ObjectId();
        ObjectId familyId = new ObjectId();
        ObjectId versionId1 = new ObjectId();
        ObjectId versionId2 = new ObjectId();
        ObjectId versionId3 = new ObjectId();

        Project project = new Project(projectId, "project", "project1");
        VersionFamily family = new VersionFamily(familyId, projectId, "1.20", null);
        Version version1 = new Version(versionId1, projectId, familyId, "1.20.1", null);
        Version version2 = new Version(versionId2, projectId, familyId, "1.20.2", null);
        Version version3 = new Version(versionId3, projectId, familyId, "1.20.3", null);
        List<Version> versions = List.of(version1, version2, version3);

        Mockito.when(this.projects.findByName("project")).thenReturn(Optional.of(project));
        Mockito.when(this.families.findByProjectAndName(projectId, "1.20")).thenReturn(Optional.of(family));
        Mockito.when(this.versions.findAllByProjectAndGroup(projectId, familyId)).thenReturn(versions);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project/version_group/1.20").accept("application/json");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project1")
                .key("version_group").value("1.20")
                .key("versions").array().value("1.20.1").value("1.20.2").value("1.20.3").endArray()
                .endObject();

        JSONAssert.assertEquals(expected.toString(), result.getResponse().getContentAsString(), false);
    }

}