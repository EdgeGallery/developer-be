/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.apitest.capability;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
import org.edgegallery.developer.service.capability.CapabilityGroupStatService;
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
public class CapabilityGroupStatQueryApiTest {

    @MockBean
    private CapabilityGroupStatService capabilityGroupService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetCapabilityGroupStatByTypeSuccess() throws Exception {
        List<CapabilityGroupStat> either = new ArrayList<>();
        String url = String.format("/mec/developer/v2/query/capability-group-stats/type/%s", "test");
        Mockito.when(capabilityGroupService.findByType(Mockito.anyString())).thenReturn(either);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
