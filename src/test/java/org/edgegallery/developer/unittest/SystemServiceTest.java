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

package org.edgegallery.developer.unittest;

import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.OpenMepCapabilityService;
import org.edgegallery.developer.service.SystemService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class SystemServiceTest {

    @Autowired
    private SystemService systemService;

    @Autowired
    private OpenMepCapabilityService openMepCapabilityService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAll() {
        Page<MepHost> res = systemService
            .getAllHosts("e111f3e7-90d8-4a39-9874-ea6ea6752ef6", "host", "10.1.12.1", 1, 0);
        Assert.assertNotNull(res);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullUserName() {
        Either<FormatRespDto, Boolean> res = systemService.createHost(new MepCreateHost(), "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullPwd() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserName("hlfonnnn");
        Either<FormatRespDto, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullUserId() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setUserId("");
        Either<FormatRespDto, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithErrorLcmIp() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithErrorConfId() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHostWithErrorId() {
        Either<FormatRespDto, Boolean> res = systemService.deleteHost("hostId");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHostSuccess() {
        Either<FormatRespDto, Boolean> res = systemService.deleteHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateHost() {
        MepCreateHost host = new MepCreateHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7", host,"");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateHostError() {
        MepCreateHost host = new MepCreateHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789", host,"");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostError() {
        Either<FormatRespDto, MepHost> res = systemService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostSuccess() {
        Either<FormatRespDto, MepHost> res = systemService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cdd");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostLogSuccess() {
        Either<FormatRespDto, List<MepHostLog>> res = systemService
            .getHostLogByHostId("d6bcf665-ba9c-4474-b7fb-25ff859563d3");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCapabilityGroupError() {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setOneLevelName("test-1");
        group.setTwoLevelName("test-2");
        group.setType(EnumOpenMepType.OPENMEP);
        List<OpenMepCapabilityDetail> list = new ArrayList<>();
        OpenMepCapabilityDetail detail1 = new OpenMepCapabilityDetail();
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
        List<OpenMepCapabilityDetail> list = new ArrayList<>();
        OpenMepCapabilityDetail detail1 = new OpenMepCapabilityDetail();
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
        List<OpenMepCapabilityDetail> list = new ArrayList<>();
        OpenMepCapabilityDetail detail1 = new OpenMepCapabilityDetail();
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
        List<OpenMepCapabilityDetail> list = new ArrayList<>();
        OpenMepCapabilityDetail detail1 = new OpenMepCapabilityDetail();
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
        Either<FormatRespDto, OpenMepCapabilityGroup> res = systemService.getCapabilityByGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        Assert.assertTrue(res.isRight());
    }


}
