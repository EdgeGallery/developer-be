package org.edgegallery.developer.apitest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.DeployService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class DeployYamlApiTest {

    @MockBean
    private DeployService deployService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSaveDeploy() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String
            .format("/mec/developer/v1/deploy/%s/action/save-yaml?userId=%s&configType=%s", "projectId", "userId",
                "upload");
        Mockito.when(deployService
            .saveDeployYaml(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson("hello"))
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateDeployYaml() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String.format("/mec/developer/v1/deploy/%s", "fileId");
        Mockito.when(deployService.updateDeployYaml(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content("content").contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYaml() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String.format("/mec/developer/v1/deploy/%s", "fileId");
        Mockito.when(deployService.getDeployYamlContent(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testQueryDeployYaml() throws Exception {
        Either<FormatRespDto, List<String>> response = Either.right(new ArrayList<>());
        String url = String.format("/mec/developer/v1/deploy/%s/action/get-json", "fileId");
        Mockito.when(deployService.getDeployYamJson(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
