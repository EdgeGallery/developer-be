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

package org.edgegallery.developer.test.controller.application.container;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.service.application.container.ContainerAppOperationService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class ContainerAppOperationCtlTest {

    @Autowired
    private ContainerAppOperationService operationService;

    // @MockBean
    // private AppOperationServiceImpl appOperationService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testInstantiateContainerAppFail() throws Exception {
        try {
            String url = String.format("/mec/developer/v2/applications/%s/containers/action/launch",
                "6a75a2bd-9811-432f-bbe8-2813aa97d367");
            mvc.perform(MockMvcRequestBuilders.post(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8));
        }catch (EntityNotFoundException e){
            Assert.assertEquals("instantiate container app fail,helmchart file id not exist.", e.getMessage());
        }
    }

}
