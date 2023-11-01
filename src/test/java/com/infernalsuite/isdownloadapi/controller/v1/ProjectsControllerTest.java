package com.infernalsuite.isdownloadapi.controller.v1;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import org.bson.types.ObjectId;
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

@WebMvcTest(value = ProjectsController.class)
public class ProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectCollection projectCollection;

    @Test
    public void givenListOfProjects_whenGetProjects_returnProjects() throws Exception {

        ObjectId id1 = new ObjectId();
        ObjectId id2 = new ObjectId();

        List<Project> projects = List.of(
                new Project(id1, "project1", "project1"),
                new Project(id2, "project2", "project2")
        );

        Mockito.when(projectCollection.findAll()).thenReturn(projects);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/projects").accept("application/json");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String expected = "{projects:[project1,project2]}";

        JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);

    }
}
