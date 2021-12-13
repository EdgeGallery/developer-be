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

package org.edgegallery.developer.test.controller.resource.pkgspec;

import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class PkgSpecCtlTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgSpecsSuccess() throws Exception {
        String url = "/mec/developer/v2/pkg-specs";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
        Assert.assertTrue(mvcResult.getResponse().getContentAsString().contains("PKG_SPEC_SUPPORT_DYNAMIC_FLAVOR"));
    }

}
