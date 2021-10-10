package org.edgegallery.developer.service.impl;

import com.spencerwi.either.Either;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.HostService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class HostServiceImplTest {
    @Autowired
    private HostService hostService;

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAll() {
        Page<MepHost> res = hostService.getAllHosts("e111f3e7-90d8-4a39-9874-ea6ea6752ef6", "host", "10.1.12.1", 1, 0);
        Assert.assertNotNull(res);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithNullUserName() {
        Either<FormatRespDto, Boolean> res = hostService.createHost(new MepHost(), "");
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
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "");
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
        host.setUserId("");
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "");
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
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "");
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
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHostWithErrorId() {
        Either<FormatRespDto, Boolean> res = hostService.deleteHost("hostId");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHostSuccess() {
        Either<FormatRespDto, Boolean> res = hostService.deleteHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7");
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
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = hostService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7", host, "");
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
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setUserName("hlfonnnn");
        host.setPassword("xxxxxxxxxxxx");
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<FormatRespDto, Boolean> res = hostService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789", host, "");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostError() {
        Either<FormatRespDto, MepHost> res = hostService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostSuccess() {
        Either<FormatRespDto, MepHost> res = hostService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cdd");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetHostLogSuccess() {
        Either<FormatRespDto, List<MepHostLog>> res = hostService
            .getHostLogByHostId("d6bcf665-ba9c-4474-b7fb-25ff859563d3");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithErrorUserId() {
        AccessUserUtil.setUser("userID", "userName", "[\"ROLE_DEVELOPER_ADMIN\"]");
        MepHost host = new MepHost();
        host.setUserName("userName");
        host.setPassword("password");
        host.setUserId("userId");
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "group");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateHostWithBadHealth() {
        MepHost host = new MepHost();
        host.setUserName("userName");
        host.setPassword("password");
        host.setUserId("admin");
        Either<FormatRespDto, Boolean> res = hostService.createHost(host, "group");
        Assert.assertTrue(res.isLeft());
    }
}
