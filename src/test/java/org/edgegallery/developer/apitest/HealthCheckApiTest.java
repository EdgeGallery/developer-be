package org.edgegallery.developer.apitest;

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import org.edgegallery.developer.controller.HealthCheck;
import org.edgegallery.developer.controller.UploadedFilesController;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.UploadFileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckApiTest {
    private Gson gson = new Gson();

    @InjectMocks
    private HealthCheck healthCheck;

    @Mock
    private UploadFileService uploadFileService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(healthCheck).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testHealthCheck() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(
            "/health")
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
