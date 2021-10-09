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

package org.edgegallery.developer.unittest;

import com.spencerwi.either.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;
import org.edgegallery.developer.model.workspace.ApplicationProjectCapability;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectCapabilityService;
import org.edgegallery.developer.service.capability.CapabilityGroupStatService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ProjectCapabilityServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectCapabilityServiceTest.class);

    @Autowired
    private ProjectCapabilityService capabilityService;

    @Test
    public void testFindByProjectIdSuccess() throws IOException {
        List<ApplicationProjectCapability> response = capabilityService.findByProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752123");
        Assert.assertNotNull(response);
    }

    @Test
    public void testCreateAppCapabilitySuccess()  {
        ApplicationProjectCapability projectCapability = new ApplicationProjectCapability();
        projectCapability.setProjectId(UUID.randomUUID().toString());
        projectCapability.setCapabilityId("e111f3e7-90d8-4a39-9874-ea6ea6752et5");
        Either<FormatRespDto, ApplicationProjectCapability> response = capabilityService.create(projectCapability);
        Assert.assertEquals(true,response.isRight());
    }

    @Test
    public void testDeleteAppCapabilitySuccess()  {
        ApplicationProjectCapability projectCapability = new ApplicationProjectCapability();
        projectCapability.setProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752123");
        projectCapability.setCapabilityId("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        Either<FormatRespDto, Boolean> response = capabilityService.delete(projectCapability);
        Assert.assertEquals(true,response.isRight());
    }

    @Test
    public void testDeleteAppCapabilityBad()  {
        ApplicationProjectCapability projectCapability = new ApplicationProjectCapability();
        projectCapability.setProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752128");
        projectCapability.setCapabilityId("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        Either<FormatRespDto, Boolean> response = capabilityService.delete(projectCapability);
        Assert.assertEquals(true,response.isLeft());
    }

    @Test
    public void testDeleteAppCapabilityByIdSuccess()  {
        Either<FormatRespDto, Boolean> response = capabilityService.deleteByProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752124");
        Assert.assertEquals(true,response.isRight());
    }

    @Test
    public void testDeleteAppCapabilityByIdError()  {
        Either<FormatRespDto, Boolean> response = capabilityService.deleteByProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752120");
        Assert.assertEquals(true,response.isLeft());

    }

    @Test
    public void testCreateAppCapabilitysSuccess()  {
        List<ApplicationProjectCapability> list = new ArrayList<>();
        ApplicationProjectCapability projectCapability = new ApplicationProjectCapability();
        projectCapability.setProjectId(UUID.randomUUID().toString());
        projectCapability.setCapabilityId("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        list.add(projectCapability);
        Either<FormatRespDto, List<ApplicationProjectCapability>> response = capabilityService.create(list);
        Assert.assertEquals(true,response.isRight());
    }
}
