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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.spencerwi.either.Either;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.UtilsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class AppTestApiTest {

    @MockBean
    private UtilsService utilsService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        //        this.mvc = MockMvcBuilders.standaloneSetup(testAppController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testQueryAllTestTaskSuccess() throws Exception {
        //        Either<FormatRespDto, TestTaskListResponse> response = Either.right(new TestTaskListResponse());
        //        Mockito.when(testAppService.getTaskByParam("", "", "", "", "123")).thenReturn(response);

        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/apps/?userId=123&appName=&status=&beginTime=&endTime=")
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testQueryAllTestTaskFail() throws Exception {
        //        Either<FormatRespDto, TestTaskListResponse> response = Either.right(new TestTaskListResponse());
        //        Mockito.when(testAppService.getTaskByParam("", "", "", "", "123")).thenReturn(response);

        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/apps/?appName=&status=&beginTime=&endTime=")
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAppTagListSuccess1() throws Exception {
        //        Either<FormatRespDto, AppTagsResponse> response = Either.right(new AppTagsResponse());
        //        Mockito.when(testAppService.getTagList()).thenReturn(response);
        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/apps/tags/?appName=").contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAppTagListSuccess2() throws Exception {
        //        Either<FormatRespDto, AppTagsResponse> response = Either.right(new AppTagsResponse());
        //        Mockito.when(testAppService.getTagList()).thenReturn(response);
        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/apps/tags/?appName=&status=&beginTime=&endTime=").contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadAppToStoreSuccess() throws Exception {
        Either<FormatRespDto, String> response = Either.right("ok");
        String url = String.format("/mec/developer/v1/apps/%s/action/upload?userId=%s&userName=%s",
            "4c22f069-e489-47cd-9c3c-e21741c857db", "test-userId", "test-userName");
        Mockito.when(utilsService.storeToAppStore(Mockito.anyMap(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("access_token", "token");
        mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).headers(httpHeaders).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.TEXT_PLAIN))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadAppToStoreFail() throws Exception {
        Either<FormatRespDto, String> response = Either.right("ok");
        String url = String.format("/mec/developer/v1/apps/%s/action/upload?userId=%s",
            "4c22f069-e489-47cd-9c3c-e21741c857db", "test-userId");
        Mockito.when(utilsService.storeToAppStore(Mockito.anyMap(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("access_token", "token");
        mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).headers(httpHeaders).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.TEXT_PLAIN))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testStartTaskSuccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/apps/4c22f069-e489-47cd-9c3c-e21741c857db/action/start-test?userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testStartTaskFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/apps/4c22f069-e489-47cd-9c3c-e21741c857db/action/start-test")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSubTaskListSuccess() throws Exception {

        //        Either<FormatRespDto, SubTaskListResponse> response = Either.right(new SubTaskListResponse());
        //        Mockito.when(testCaseService.getSubtasks("fd497d95-7c98-40cb-bc90-308bdefc0e39",
        //                "11e12b66-508f-48d4-bbc8-3ed99631cf92")).thenReturn(response);
        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/apps/fd497d95-7c98-40cb-bc90-308bdefc0e39/task/11e12b66-508f-48d4-bbc8-3ed99631cf92/subtasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSubTaskListFail() throws Exception { // appId or taskId invalid

        //        Either<FormatRespDto, SubTaskListResponse> response =
        //                Either.left(new FormatRespDto(Status.BAD_REQUEST, "appId or taskId invalid"));
        //        Mockito.when(testCaseService.getSubtasks("fd497d95-7c98-40cb-bc90-308bdefc0e39999",
        //                "11e12b66-508f-48d4-bbc8-3ed99631cf92")).thenReturn(response);
        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/apps/fd497d95-7c98-40cb-bc90-308bdefc0e39999/task/11e12b66-508f-48d4-bbc8-3ed99631cf92/subtasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
