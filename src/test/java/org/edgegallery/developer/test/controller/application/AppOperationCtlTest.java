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

package org.edgegallery.developer.test.controller.application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.service.apppackage.csar.signature.EncryptedService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.AtpUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class AppOperationCtlTest {

    @Autowired
    private MockMvc mvc;

    Gson gson = new Gson();

    @Autowired
    EncryptedService encryptedService;

    @Autowired
    UploadFileService uploadFileService;

    @Before
    public void init() {
        new MockUp<AccessUserUtil>() {
            @Mock
            public User getUser() {
                return new User("userId", "userName");
            }
        };
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void createAtpTestTest() throws Exception {
        new MockUp<AtpUtil>() {
            @Mock
            public String sendCreateTask2Atp(String filePath, String token) {
                Map<String, String> result = new HashMap<String, String>();
                result.put("id", "6a75a2bd-1111-432f-bbe8-2813aa97d375");
                result.put("status", "created");
                result.put("createTime", "2021");
                result.put("appName", "appName");
                return gson.toJson(result).toString();
            }
        };

        String url = String
            .format("/mec/developer/v2/applications/%s/action/atp-tests", "6a75a2bd-9811-432f-bbe8-2813aa97d365");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with((csrf())))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void getAtpTestsTest() throws Exception {
        mockQueryAtpTests();
        String url = String
            .format("/mec/developer/v2/applications/%s/action/atp-tests", "6a75a2bd-9811-432f-bbe8-2813aa97d365");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).with((csrf())))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void getAtpTestByIdTest() throws Exception {
        mockQueryAtpTests();
        String url = String
            .format("/mec/developer/v2/applications/%s/atpTests/%s", "6a75a2bd-9811-432f-bbe8-2813aa97d365",
                "6a75a2bd-1111-432f-bbe8-2813aa97d365");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).with((csrf())))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    private void mockQueryAtpTests() {
        new MockUp<AtpUtil>() {
            @Mock
            public String getTaskStatusFromAtp(String taskId) {
                Map<String, String> result = new HashMap<String, String>();
                result.put("id", "6a75a2bd-1111-432f-bbe8-2813aa97d365");
                result.put("status", "success");
                result.put("createTime", "2021");
                result.put("appName", "appName");
                return gson.toJson(result).toString();
            }
        };
    }
}
