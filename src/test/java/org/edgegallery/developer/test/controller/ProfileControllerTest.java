/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.test.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.io.File;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.profile.ProfileInfo;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.UploadFileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    Gson gson = new Gson();

    private static final String UUID = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";

    private String profileId;

    @Before
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void createProfileTest() throws Exception {
        File file = Resources.getResourceAsFile("testdata/profile.zip");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v2/profiles").file(
            new MockMultipartFile("file", "profile.zip", MediaType.TEXT_PLAIN_VALUE, FileUtils.openInputStream(file)))
            .with(csrf())).andReturn();
        assertEquals(200, mvcResult.getResponse().getStatus());

        String content = mvcResult.getResponse().getContentAsString();
        ProfileInfo profileInfo = gson.fromJson(content, ProfileInfo.class);
        profileId = profileInfo.getId();
    }

    @After
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void deleteProfileByIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v2/profiles/".concat(profileId)).with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @After
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void deleteProfileByIdNotExistTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v2/profiles/".concat(UUID)).with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void downloadProfileFileTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v2/profiles/".concat(profileId))
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void downloadDeployFileTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v2/profiles/".concat(profileId))
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).param("type", "deployFile")
            .param("name", "fledge").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void downloadConfigFileTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v2/profiles/".concat(profileId))
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).param("type", "configFile")
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void getProfilesTest() throws Exception {
        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v2/profiles/").contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf()).param("limit", "10").param("offset", "0").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void getProfileByIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v2/profiles/".concat(profileId))
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void getProfileByIdNotExistTest() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v2/profiles/".concat(UUID))
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void createAppByProfileIdTest() throws Exception {
        new MockUp<AccessUserUtil>() {
            @Mock
            public User getUser() {
                return new User("userId", "userName");
            }
        };

        new MockUp<ContainerAppHelmChartUtil>() {
            @Mock
            public boolean checkImageExist(List<String> imageList) {
                return true;
            }
        };

        File file = Resources.getResourceAsFile("testdata/face.png");
        mvc.perform(MockMvcRequestBuilders
            .multipart("/mec/developer/v2/profiles/".concat(profileId).concat("/create-application")).file(
                new MockMultipartFile("iconFile", "iconFile.jpg", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(file))).with(csrf())).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void createAppByProfileIdNameVersionExistsTest() throws Exception {
        new MockUp<AccessUserUtil>() {
            @Mock
            public User getUser() {
                return new User("userId", "userName");
            }
        };

        File file = Resources.getResourceAsFile("testdata/face.png");
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .multipart("/mec/developer/v2/profiles/".concat(profileId).concat("/create-application")).file(
                new MockMultipartFile("iconFile", "iconFile.jpg", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(file))).with(csrf())).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }
}
