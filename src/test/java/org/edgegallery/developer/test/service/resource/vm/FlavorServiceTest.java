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

package org.edgegallery.developer.test.service.resource.vm;

import java.util.List;
import org.edgegallery.developer.model.resource.vm.Flavor;
import org.edgegallery.developer.service.recource.vm.FlavorService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class FlavorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlavorServiceTest.class);

    @Autowired
    private FlavorService flavorService;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");

    }

    @Test
    public void testGetAllFavorsSuccess() {
        List<Flavor> flavors = flavorService.getAllFavors();
        Assert.assertNotNull(flavors);
    }

    @Test
    public void testGetFavorByIdSuccess() {
        Flavor flavor = flavorService.getFavorById("3ef2bea0-5e23-4fab-952d-cc9e6741dbe7");
        Assert.assertNotNull(flavor);
    }

    @Test
    public void testCreateFavorSuccess() {
        Flavor flavor = new Flavor();
        flavor.setName("flavor-name");
        flavor.setDescription("desc");
        flavor.setArchitecture("X86");
        flavor.setCpu(1);
        flavor.setMemory(2);
        flavor.setSystemDiskSize(100);
        flavor.setDataDiskSize(50);
        flavor.setGpuExtraInfo("gpuInfo");
        flavor.setOtherExtraInfo("otherInfo");
        Flavor createdFlavor = flavorService.createFavor(flavor);
        Assert.assertNotNull(createdFlavor);
    }

    @Test
    public void testDeleteFavorSuccess() {
        Flavor flavor = new Flavor();
        flavor.setName("flavor-name");
        flavor.setDescription("desc");
        flavor.setArchitecture("X86");
        flavor.setCpu(1);
        flavor.setMemory(2);
        flavor.setSystemDiskSize(100);
        flavor.setDataDiskSize(50);
        flavor.setGpuExtraInfo("gpuInfo");
        flavor.setOtherExtraInfo("otherInfo");
        Flavor createdFlavor = flavorService.createFavor(flavor);
        Assert.assertNotNull(createdFlavor);
        boolean res = flavorService.deleteFavorById(createdFlavor.getId());
        Assert.assertEquals(true, res);
    }

}
