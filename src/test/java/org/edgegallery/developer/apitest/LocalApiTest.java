/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.apitest;

import com.spencerwi.either.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.controller.LocalApiController;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SwaggerService;
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
public class LocalApiTest {

    @InjectMocks
    private LocalApiController localApiController;

    @Mock
    private SwaggerService swaggerService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(localApiController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAllApi() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> response = Either
            .right(ResponseEntity.status(HttpStatus.OK).build());
        Mockito.when(swaggerService.getFile("plugin")).thenReturn(response);

        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/localapi/plugin").accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = AssertionError.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetApiFailed() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "fileName is invalid."));
        Mockito.when(swaggerService.getFile("plugin-001")).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/localapi/plugin-001")
            .accept(MediaType.APPLICATION_OCTET_STREAM)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
