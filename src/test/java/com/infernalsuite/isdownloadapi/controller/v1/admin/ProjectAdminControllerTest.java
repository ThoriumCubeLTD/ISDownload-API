package com.infernalsuite.isdownloadapi.controller.v1.admin;

import com.infernalsuite.isdownloadapi.database.model.Project;
import com.infernalsuite.isdownloadapi.database.repository.ProjectCollection;
import com.sun.source.tree.BinaryTree;
import org.bson.types.ObjectId;
import org.json.JSONStringer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ProjectAdminController.class)
class ProjectAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCollection projectCollection;

    @Test
    void givenProject_whenCreateProject_returnProject() throws Exception {

        ObjectId projectId = new ObjectId();
        Project project = new Project(projectId, "project", "project");

        Mockito.when(projectCollection.save(Mockito.any())).thenReturn(project);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/admin/projects")
                .content(new JSONStringer()
                        .object()
                        .key("name").value("project")
                        .key("description").value("project")
                        .endObject()
                        .toString())
                .contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"));

    }
}