package org.edgegallery.developer.apitest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ReleaseConfigService;
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
public class ReleaseConfigApiTest {

    @MockBean
    private ReleaseConfigService configService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfig() throws Exception {
        Either<FormatRespDto, ReleaseConfig> response = Either.right(new ReleaseConfig());
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithNullId() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithNoTestConfig() throws Exception {
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Project has not test config!");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(error);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithErrorPath() throws Exception {
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Cannot find csar file: hello");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(error);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithErrorTmpFile() throws Exception {
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Cannot find service template file.");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(error);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithExe() throws Exception {
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Update csar failed: hello");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(error);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateRelConfigWithInternalError() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.saveConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        Assert.assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyRelConfig() throws Exception {
        Either<FormatRespDto, ReleaseConfig> response = Either.right(new ReleaseConfig());
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.modifyConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyRelConfigWithNullId() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.modifyConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyRelConfigWithErrorId() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId error!");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.modifyConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyRelConfigWithInterError() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "modify config data fail");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.modifyConfig(Mockito.anyString(), Mockito.any())).thenReturn(response);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(new ReleaseConfig()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        Assert.assertEquals(500, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetRelConfig() throws Exception {
        Either<FormatRespDto, ReleaseConfig> response = Either.right(new ReleaseConfig());
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.getConfigById(Mockito.anyString())).thenReturn(response);
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, result.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetRelConfigWithNullId() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
        Either<FormatRespDto, ReleaseConfig> response = Either.left(dto);
        String url = String
            .format("/mec/developer/v1/releaseconfig/%s/action/release-config", "4c22f069-e489-47cd-9c3c-e21741c857db");
        Mockito.when(configService.getConfigById(Mockito.anyString())).thenReturn(response);
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isBadRequest());
        Assert.assertEquals(400, result.andReturn().getResponse().getStatus());
    }
}
