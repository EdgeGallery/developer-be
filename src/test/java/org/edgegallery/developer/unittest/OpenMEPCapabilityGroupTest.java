/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroups;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.OpenMepCapabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class OpenMEPCapabilityGroupTest {

    @Autowired
    private OpenMepCapabilityService openMEPCapabilityService;

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getMEPCapabilities() {
        OpenMepCapabilityGroups all = openMEPCapabilityService.getAll(null);
        Assert.assertNotNull(all);
        Assert.assertNotNull(all.getValues());
        Assert.assertEquals(all.getValues().size(), 7);
        Assert.assertNull(all.getValues().get(0).getCapabilityDetailList());
        all = openMEPCapabilityService.getAll("detail");
        Assert.assertNotNull(all);
        Assert.assertNotNull(all.getValues());
        Assert.assertEquals(all.getValues().size(), 7);
        Assert.assertNotNull(all.getValues().get(0).getCapabilityDetailList());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllCapabilityGroups() {
        Either<FormatRespDto, List<OpenMepCapabilityGroup>> response = openMEPCapabilityService
            .getAllCapabilityGroups();
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getCapabilitiesByGroupId() {
        Either<FormatRespDto, OpenMepCapabilityGroup> response = openMEPCapabilityService
            .getCapabilitiesByGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        Assert.assertTrue(response.isRight());

        response = openMEPCapabilityService.getCapabilitiesByGroupId("a6efaa2c-ad99-432f-9405-ewwwwwwwwwwwwwwwww");
        Assert.assertTrue(response.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createGroup() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752e44");
        group.setOneLevelName("Face recognition");
        group.setType(EnumOpenMepType.OPENMEP);
        group.setDescription("face recognition");
        Either<FormatRespDto, OpenMepCapabilityGroup> response = openMEPCapabilityService.createGroup(group);
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createCapability() {
        OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail();
        detail.setGroupId("c0db376b-ae50-48fc-b9f7-58a609e3ee12");
        detail.setService("Face Recognition Service New");
        detail.setServiceEn("Face Recognition Service New");
        detail.setVersion("v2");
        detail.setDescription("provide the face recognition capabilities for apps");
        detail.setDescriptionEn("provide the face recognition capabilities for apps");
        detail.setProvider("Huawei");
        detail.setApiFileId("9f1f13a0-8554-4dfa-90a7-d2765238fca7");
        detail.setGuideFileId("9f1f13a0-8554-4dfa-90a7-d2765238fca7");
        detail.setGuideFileIdEn("9f1f13a0-8554-4dfa-90a7-d2765238fca7");
        detail.setUserId("d0f8fa57-2f4c-4182-be33-0a508964d0");
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        detail.setUploadTime(time.format(new Date()));
        detail.setDetailId("e111f3e7-90d8-4a39-9874-ea6ea6752ef4");
        Either<FormatRespDto, OpenMepCapabilityDetail> response = openMEPCapabilityService
            .createCapability("c0db376b-ae50-48fc-b9f7-58a609e3ee12", detail);
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteGroup() {
        Either<FormatRespDto, Boolean> response = openMEPCapabilityService
            .deleteGroup("e111f3e7-90d8-4a39-9874-ea6ea6752e44");
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteCapability() {
        Either<FormatRespDto, Boolean> response = openMEPCapabilityService
            .deleteCapabilityByUserId("e111f3e7-90d8-4a39-9874-ea6ea6752ef4", "d0f8fa57-2f4c-4182-be33-0a508964d0");
        Assert.assertTrue(response.isRight());
    }
}
