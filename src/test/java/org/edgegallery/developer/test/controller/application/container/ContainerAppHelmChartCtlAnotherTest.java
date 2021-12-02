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


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.junit.Assert;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class ContainerAppHelmChartCtlAnotherTest {
    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() throws IOException {
        new MockUp<ContainerAppHelmChartUtil>() {
            @Mock
            public boolean checkImageExist(List<String> imageList) {
                {
                    return true;
                }
            }
        };

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadHelmChartFile1Success() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts", UUID.randomUUID().toString());
        File file = Resources.getResourceAsFile("testdata/helmcharts/bonita.yaml");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.multipart(url).file(
            new MockMultipartFile("file", "bonita.yaml", MediaType.TEXT_PLAIN_VALUE, FileUtils.openInputStream(file)))
            .with(csrf())).andReturn();
        Assert.assertEquals(200, mvcResult.getResponse().getStatus());

    }

}
