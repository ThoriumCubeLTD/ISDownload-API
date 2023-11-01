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

@WebMvcTest(value = ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectCollection projectCollection;
    @MockBean
    private VersionFamilyCollection versionFamilyCollection;
    @MockBean
    private VersionCollection versionCollection;

    @Test
    public void givenListOfProjects_whenGetProjectsByName_returnProjectsByName() throws Exception {

        ObjectId id1 = new ObjectId();

        Project project1 = new Project(id1, "project", "project1");

        ObjectId versionFamilyId1 = new ObjectId();
        VersionFamily versionFamily = new VersionFamily(versionFamilyId1, id1, "1.20", null);

        ObjectId versionId1 = new ObjectId();
        Version version = new Version(versionId1, id1, versionFamilyId1, "1.20.1", null);

        Mockito.when(projectCollection.findByName(Mockito.anyString())).thenReturn(Optional.of(project1));
        Mockito.when(versionFamilyCollection.findAllByProject(Mockito.any(ObjectId.class))).thenReturn(List.of(versionFamily));
        Mockito.when(versionCollection.findAllByProject(Mockito.any(ObjectId.class))).thenReturn(List.of(version));


        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects/project").accept("application/json");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        System.out.println(result.getResponse().getContentAsString());

        JSONStringer expected = new JSONStringer();
        expected.object()
                .key("project_id").value("project")
                .key("project_name").value("project1")
                .key("version_groups").array().value("1.20").endArray()
                .key("versions").array().value("1.20.1").endArray()
                .endObject();
        System.out.println(expected);

        String expectedString = expected.toString();

        JSONAssert.assertEquals(expectedString, result.getResponse().getContentAsString(), false);


    }

}