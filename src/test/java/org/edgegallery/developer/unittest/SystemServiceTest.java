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
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
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
        Either<FormatRespDto, MepHost> res = systemService.createHost(new MepHost(), "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullPwd() {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserName("hlfonnnn");
        Either<FormatRespDto, MepHost> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullUserId() {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        Either<FormatRespDto, MepHost> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithErrorLcmIp() {
        MepHost host = new MepHost();
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
        Either<FormatRespDto, MepHost> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithErrorConfId() {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("119.8.47.5");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, MepHost> res = systemService.createHost(host, "");
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
        MepHost host = new MepHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("119.8.47.11");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, MepHost> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7", host,"");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateHostError() {
        MepHost host = new MepHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("119.8.47.5");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, MepHost> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789", host,"");
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
