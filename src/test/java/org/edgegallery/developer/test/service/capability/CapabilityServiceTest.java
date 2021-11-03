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

package org.edgegallery.developer.test.service.capability;

import com.spencerwi.either.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.capability.CapabilityService;
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
public class CapabilityServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityServiceTest.class);

    @Autowired
    private CapabilityService capabilityService;

    @Test
    public void testCreateCapabilityWithBadName() throws IOException {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("");
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testCreateCapabilityWithBadApiId() throws IOException {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("test");
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testCreateCapabilityWithBadGuideId() throws IOException {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("test");
        capability.setApiFileId("apiId");
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testCreateCapabilityWithBadIconId() throws IOException {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("test");
        capability.setApiFileId("apiId");
        capability.setGuideFileId("guideId");
        capability.setGuideFileIdEn("guideIdEn");
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testCreateCapabilityWithNullGroup() throws IOException {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("test");
        capability.setApiFileId("apiId");
        capability.setGuideFileId("guideId");
        capability.setGuideFileIdEn("guideIdEn");
        capability.setIconFileId("iconFileID");
        capability.setGroup(null);
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isRight());
    }

    @Test
    public void testCreateCapabilityWithNullGroupId() throws IOException {
        try {
            Capability capability = new Capability();
            capability.setId(UUID.randomUUID().toString());
            capability.setName("test");
            capability.setApiFileId("apiId");
            capability.setGuideFileId("guideId");
            capability.setGuideFileIdEn("guideIdEn");
            capability.setIconFileId("iconFileID");
            CapabilityGroup group = new CapabilityGroup();
            group.setId(null);
            group.setName("group-2");
            group.setNameEn("group-2");
            capability.setGroup(group);
            Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        } catch (DeveloperException e) {
            Assert.assertEquals("update api or guide or guide-en or icon file status occur db error", e.getMessage());
        }
    }

    @Test
    public void testCreateCapabilityWithNullGroupId1() throws IOException {
        try {
            Capability capability = new Capability();
            capability.setId(UUID.randomUUID().toString());
            capability.setName("test");
            capability.setApiFileId("apiId");
            capability.setGuideFileId("guideId");
            capability.setGuideFileIdEn("guideIdEn");
            capability.setIconFileId("iconFileID");
            CapabilityGroup group = new CapabilityGroup();
            group.setId(null);
            group.setName("group-4");
            group.setNameEn("group-4");
            capability.setGroup(group);
            Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        } catch (DeveloperException e) {
            Assert.assertEquals("update api or guide or guide-en or icon file status occur db error", e.getMessage());
        }
    }

    @Test
    public void testCreateCapabilityWithGroupId() {
        Capability capability = new Capability();
        capability.setId(UUID.randomUUID().toString());
        capability.setName("test");
        capability.setApiFileId("apiId");
        capability.setGuideFileId("guideId");
        capability.setGuideFileIdEn("guideIdEn");
        capability.setIconFileId("iconFileID");
        CapabilityGroup group = new CapabilityGroup();
        group.setId("xxxxx");
        group.setName("group-4");
        group.setNameEn("group-4");
        capability.setGroup(group);
        Either<FormatRespDto, Capability> response = capabilityService.create(capability);
        Assert.assertEquals(true, response.isLeft());
    }

    @Test
    public void testDeleteCapabilityWithBadId() {
        Either<FormatRespDto, Capability> either = capabilityService.deleteById("test");
        Assert.assertEquals(true, either.isRight());
    }

    @Test
    public void testDeleteCapabilityWithRightId() {
        Either<FormatRespDto, Capability> either = capabilityService.deleteById("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        Assert.assertEquals(true, either.isRight());
    }

    @Test
    public void testDeleteCapabilityWithRightId1() {
        Either<FormatRespDto, Capability> either = capabilityService.deleteById("e111f3e7-90d8-4a39-9874-ea6ea6752e99");
        Assert.assertEquals(true, either.isRight());
    }

    @Test
    public void testFindAllCapability() {
        List<Capability> either = capabilityService.findAll();
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityByType() {
        List<Capability> either = capabilityService.findByType("OPENMEP");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindCapabilityById() {
        Capability either = capabilityService.findById("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityByGroupId() {
        List<Capability> either = capabilityService.findByGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752et4");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityByProjectId() {
        List<Capability> either = capabilityService.findByProjectId("e111f3e7-90d8-4a39-9874-ea6ea6752123");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityByName() {
        List<Capability> either = capabilityService.findByNameWithFuzzy("Face Reg");
        Assert.assertNotNull(either);
    }

    @Test
    public void testFindAllCapabilityByNameEn() {
        List<Capability> either = capabilityService.findByNameEnWithFuzzy("Face Reg");
        Assert.assertNotNull(either);
    }

    @Test
    public void testUpdateSelectCountByIds() {
        List<String> ids = new ArrayList<>();
        boolean either = capabilityService.updateSelectCountByIds(ids);
        Assert.assertEquals(either,true);
    }

    @Test
    public void testUpdateSelectCountByIds1() {
        List<String> ids = new ArrayList<>();
        ids.add("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        boolean either = capabilityService.updateSelectCountByIds(ids);
        Assert.assertEquals(either,true);
    }

}
