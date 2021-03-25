package org.edgegallery.developer.apitest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.DeployPlatformConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ConfigService;
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
public class ConfigApiTest {

    @MockBean
    private ConfigService configService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployPlatformConfig() throws Exception {
        Either<FormatRespDto, DeployPlatformConfig> response = Either.right(new DeployPlatformConfig());
        String url = String.format("/mec/developer/v1/config/deploy-platform");
        Mockito.when(configService.configDeployPlatform(Mockito.any())).thenReturn(response);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(new DeployPlatformConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployPlatformConfig() throws Exception {
        Either<FormatRespDto, DeployPlatformConfig> response = Either.right(new DeployPlatformConfig());
        String url = String.format("/mec/developer/v1/config/deploy-platform");
        Mockito.when(configService.getConfigDeployPlatform()).thenReturn(response);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
