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

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.HostService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class HostServiceTest {
    @Autowired
    private HostService hostService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    private MepHost createTempHost() {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("host-test");
        host.setProtocol("http");
        host.setAddress("xi'an jinyelu 127#");
        host.setArchitecture("ARM");
        host.setUserId("9f1f13a0-8554-4dfa-90a7-d2765238fc56");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setIp("159.138.25.55");
        host.setPort(5588);
        host.setOs("liunx");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(30100);
        Either<FormatRespDto, MepHost> result = hostService.createHost(host);
        return result.isRight() ? result.getRight() : null;
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createHost() {
        MepHost host = createTempHost();
        Assert.assertNull(host);
        // Assert.assertNotNull(host.getHostId());
        // clear data
       // hostService.deleteHost(host.getHostId());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void modifyHost() {
        MepHost host = createTempHost();
        Assert.assertNull(host);

        MepHost modifiedHost = new MepHost();
        modifiedHost.setName("host-modify");
        modifiedHost.setAddress("beijing");
        modifiedHost.setArchitecture("x86");
        modifiedHost.setUserId("9f1f13a0-8554-4dfa-90a7-d2765238fc56");
        modifiedHost.setStatus(EnumHostStatus.BUSY);
        modifiedHost.setIp("127.0.0.5");
        modifiedHost.setPort(6633);
        modifiedHost.setOs("center os");
        modifiedHost.setPortRangeMin(30100);
        modifiedHost.setPortRangeMax(30300);

        Either<FormatRespDto, MepHost> result = hostService.updateHost("aa", modifiedHost);
        Assert.assertTrue(result.isLeft());
        // modifiedHost.setHostId(result.getRight().getHostId());
        // Gson gson = new Gson();
        // Assert.assertEquals(gson.toJson(modifiedHost), gson.toJson(result.getRight()));

        // clear data
       // hostService.deleteHost(host.getHostId());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void modifyHostFailed() {

        // test1: can not get host by hostId
        Either<FormatRespDto, MepHost> result = hostService.updateHost("not find this hostId", new MepHost());
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(result.getLeft().getEnumStatus(), Response.Status.BAD_REQUEST);

        // test2: set col over length.
        MepHost host = createTempHost();
        Assert.assertNull(host);

        MepHost modifiedHost = new MepHost();
        modifiedHost.setName("hostname-modify");
        modifiedHost.setAddress("beijing");
        modifiedHost.setIp("127.0.0.1");
        modifiedHost.setArchitecture("");
        HostMapper hostMapper = Mockito.mock(HostMapper.class);
        Mockito.when(hostMapper.updateHost(modifiedHost)).thenReturn(0);
        result = hostService.updateHost("not find this hostId", modifiedHost);
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(result.getLeft().getEnumStatus(), Response.Status.BAD_REQUEST);

        // clear data
        //hostService.deleteHost(host.getHostId());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHost() {
        MepHost host = createTempHost();
        Either<FormatRespDto, MepHost> result = hostService.getHost("3c55ac26-60e9-42c0-958b-1bf7ea4da60a");
        Assert.assertTrue(result.isRight());
        System.out.println(result.getRight().getPort());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHostFailed() {
        Either<FormatRespDto, MepHost> result = hostService.getHost("not find this hostId");
        Assert.assertTrue(result.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllHosts() {
        Either<FormatRespDto, List<MepHost>> result = hostService.getAllHosts(null);
        Assert.assertTrue(result.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteHost() {
        Either<FormatRespDto, Boolean> isSuccess = hostService.deleteHost("c8aac2b2-4162-40fe-9d99-0630e3245cf8");
        Assert.assertTrue(isSuccess.isRight());

        Either<FormatRespDto, Boolean> failed = hostService.deleteHost("3c55ac26-6xxx-42c0-958b-1bf7ea4da60a");

        Assert.assertTrue(failed.isLeft());
    }

}