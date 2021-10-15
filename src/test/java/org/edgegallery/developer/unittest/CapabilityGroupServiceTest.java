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
import java.util.List;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
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
public class CapabilityGroupServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityGroupServiceTest.class);

    @Autowired
    private CapabilityGroupService groupService;

    @Test
    public void testCreateCapabilityGroupSuccess() throws IOException {
        CapabilityGroup group = new CapabilityGroup();
        group.setName("test-123");
        group.setDescription("desc");
        Either<FormatRespDto, CapabilityGroup> response = groupService.create(group);
        Assert.assertEquals(true, response.isRight());
    }

    @Test
    public void testDeleteCapabilityGroupSuccess() throws IOException {
        Either<FormatRespDto, String> response = groupService.deleteById("e111f3e7-90d8-4a39-9874-ea6ea6752et6");
        Assert.assertEquals(true, response.isRight());
    }

    @Test
    public void testDeleteCapabilityGroupBad() throws IOException {
        Either<FormatRespDto, String> response = groupService.deleteById("e111f3e7-90d8-4a39-9874-ea6ea6752et9");
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testFindAllCapabilityGroup() {
        List<CapabilityGroup> either = groupService.findAll();
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityGroupByType() {
        List<CapabilityGroup> either = groupService.findByType("OPENMEP");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindCapabilityById() {
        CapabilityGroup either = groupService.findById("e111f3e7-90d8-4a39-9874-ea6ea6752et5");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindCapabilityByName() {
        List<CapabilityGroup> either = groupService.findByNameOrNameEn("group-2","group-2");
        Assert.assertNotNull(either);
    }

}
