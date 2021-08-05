/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.impl;

import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapability;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.OpenMepCapabilityService;
import org.edgegallery.developer.service.CapabilityService;
import org.edgegallery.developer.service.impl.HostServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class CapabilityServiceImplTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityServiceImplTest.class);
	
    @Autowired
    private CapabilityService systemService;

    @Autowired
    private OpenMepCapabilityService openMepCapabilityService;

    @Before
    public void init() {
    	LOGGER.info("start to test");
    }

    @After
    public void after() {
    	LOGGER.info("test over");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDleteCapabilityByUserIdAndGroupId() {
        Either<FormatRespDto, Boolean> res = systemService.deleteCapabilityByUserIdAndGroupId("group");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDleteCapabilityWithGid() {
        Either<FormatRespDto, Boolean> res = systemService
            .deleteCapabilityByUserIdAndGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752eb2");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroupError() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setOneLevelName("test-1");
        group.setTwoLevelName("test-2");
        group.setType(EnumOpenMepType.OPENMEP);
        List<OpenMepCapability> list = new ArrayList<>();
        OpenMepCapability detail1 = new OpenMepCapability();
        detail1.setApiFileId("");
        list.add(detail1);
        group.setCapabilityDetailList(list);
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.createCapabilityGroup(group);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroupError2() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setOneLevelName("test-1");
        group.setTwoLevelName("test-2");
        group.setType(EnumOpenMepType.OPENMEP);
        List<OpenMepCapability> list = new ArrayList<>();
        OpenMepCapability detail1 = new OpenMepCapability();
        detail1.setApiFileId("apifileId");
        detail1.setGuideFileId("");
        list.add(detail1);
        group.setCapabilityDetailList(list);
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.createCapabilityGroup(group);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroupError3() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setOneLevelName("test-1");
        group.setTwoLevelName("test-2");
        group.setType(EnumOpenMepType.OPENMEP);
        List<OpenMepCapability> list = new ArrayList<>();
        OpenMepCapability detail1 = new OpenMepCapability();
        detail1.setApiFileId("apifileId");
        detail1.setGuideFileId("guide");
        detail1.setGuideFileIdEn("guideFileEN");
        list.add(detail1);
        group.setCapabilityDetailList(list);
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.createCapabilityGroup(group);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroup() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setOneLevelName("test-1");
        group.setTwoLevelName("test-2");
        group.setType(EnumOpenMepType.OPENMEP);
        List<OpenMepCapability> list = new ArrayList<>();
        OpenMepCapability detail1 = new OpenMepCapability();
        detail1.setApiFileId("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        detail1.setGuideFileId("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        detail1.setGuideFileIdEn("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        list.add(detail1);
        group.setCapabilityDetailList(list);
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.createCapabilityGroup(group);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroupFail() {
        Either<FormatRespDto, OpenMepCapabilityGroup> resGroup = openMepCapabilityService
            .getCapabilitiesByGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ed6");
        Assert.assertTrue(resGroup.isRight());
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.createCapabilityGroup(resGroup.getRight());
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteCapabilityByUserIdAndGroupId() {
        Either<FormatRespDto, Boolean> res = systemService.deleteCapabilityByUserIdAndGroupId("resGroup.getRight()");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteCapabilityByUserIdAndGroupIdS() {
        Either<FormatRespDto, Boolean> res = systemService
            .deleteCapabilityByUserIdAndGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAllCapabilityGroups() {
        Page<OpenMepCapabilityGroup> res = systemService.getAllCapabilityGroups("admin", "group-2", "group-2-en", 1, 0);
        Assert.assertNotNull(res);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetCapabilityByGroupId() {
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.getCapabilityByGroupId("xxxx");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetCapabilityByGroupIdSuccess() {
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService
            .getCapabilityByGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        Assert.assertTrue(res.isRight());
    }

}
