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

package org.edgegallery.developer.test.service.resource.mephost;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.model.resource.mephost.EnumMepHostStatus;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.resource.mephost.MepHostLog;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MepHostServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MepHostServiceTest.class);

    @Autowired
    private MepHostService mepHostService;

    private MockHttpServletRequest request;

    private HttpServer httpServer;

    private boolean isConfig = false;

    @Before
    public void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 31252), 0);
        httpServer.createContext("/lcmcontroller/v1/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    String res = "ok";
                    byte[] response = res.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/lcmcontroller/v1/hosts", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    isConfig = true;
                }
                exchange.close();
            }
        });
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    public void testGetAllHostsSuccess() {
        Page<MepHost> page = mepHostService.getAllHosts("", "", "", 10, 0);
        Assert.assertNotNull(page);
    }

    @Test
    public void testCreateHostBadWithExistHost() {
        try {
            mepHostService.createHost(createNewHost(), null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("mecHost already exists!", e.getMessage());
        }
    }

    @Test
    public void testCreateHostBadWithErrAuth() {
        try {
            //Consts.ROLE_DEVELOPER_ADMIN
            AccessUserUtil.setUser("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", "");
            mepHostService.createHost(createAnotherHost(), AccessUserUtil.getUser());
        } catch (UnauthorizedException e) {
            Assert.assertEquals("userId is empty or not admin!", e.getMessage());
        }
    }

    @Test
    public void testCreateHostBadWithErrNetConfig() {
        try {
            AccessUserUtil.setUser("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN);
            mepHostService.createHost(createAnotherHost(), AccessUserUtil.getUser());
        } catch (IllegalRequestException e) {
            Assert.assertEquals("Network params config error!", e.getMessage());
        }
    }

    @Test
    public void testCreateHostBadWithErrNginxConfig() throws Exception {
        try {
            AccessUserUtil.setUser("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN);
            User user = new User("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN, "token");
            isConfig = false;
            mepHostService.createHost(createHost(), user);
        } catch (DeveloperException e) {
            Assert.assertEquals("add mec host to lcm fail!", e.getMessage());
        }
    }

    @Test
    public void testDeleteHostSuccess() throws Exception {
        boolean res = mepHostService.deleteHost("fe934a92-1cfc-42fe-919d-422e2e3bd1f8");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testUpdateHostSuccess() throws Exception {
        try {
            AccessUserUtil.setUser("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN);
            User user = new User("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN, "token");
            isConfig = false;
            mepHostService.updateHost("fe934a92-1cfc-42fe-919d-422e2e3bd1f9", createHost(), user);
        }catch (DeveloperException e){
            Assert.assertEquals("add mec host to lcm fail!",e.getMessage());
        }

    }

    @Test
    public void testUpdateHostBadWithErrId() throws Exception {
        try {
            mepHostService.updateHost("hostId", null, null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("Can not find the host!", e.getMessage());
        }
    }

    @Test
    public void testHostBadWithErrId() throws Exception {
        try {
            mepHostService.getHost("hostId");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("can not find the host!", e.getMessage());
        }
    }

    @Test
    public void testGetHostSuccess() throws Exception {
        MepHost mepHost = mepHostService.getHost("fe934a92-1cfc-42fe-919d-422e2e3bd1f9");
        Assert.assertNotNull(mepHost);
    }

    @Test
    public void testGetHostLogsSuccess() throws Exception {
        List<MepHostLog> logs = mepHostService.getHostLogByHostId("fe934a92-1cfc-42fe-919d-422e2e3bd1f9");
        Assert.assertEquals(0, logs.size());
    }

    @Test
    public void testUploadHostConfigFileBadWithErrName() throws Exception {
        try {
            MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
                MepHostServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
            mepHostService.uploadConfigFile(UUID.randomUUID().toString(),uploadFile);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("upload file should not have suffix", e.getMessage());
        }
    }

    @Test
    public void testUploadHostConfigFileSuccess() throws Exception {
        AccessUserUtil.setUser("5ce78873-d73d-4e7d-84a4-ab75ac95400f", "admin", Consts.ROLE_DEVELOPER_ADMIN);
        MultipartFile uploadFile = new MockMultipartFile("config", "config", null,
            MepHostServiceTest.class.getClassLoader().getResourceAsStream("testdata/config"));
        UploadFile uploadedFile = mepHostService.uploadConfigFile(UUID.randomUUID().toString(),uploadFile);
        Assert.assertNotNull(uploadedFile);
    }

    private MepHost createNewHost() {
        MepHost mepHost = new MepHost();
        mepHost.setId(UUID.randomUUID().toString());
        mepHost.setName("k8s-test");
        mepHost.setLcmIp("1.1.1.1");
        mepHost.setLcmProtocol("https");
        mepHost.setLcmPort(30100);
        mepHost.setArchitecture("X86");
        mepHost.setStatus(EnumMepHostStatus.NORMAL);
        mepHost.setMecHostIp("1.1.1.1");
        mepHost.setVimType(EnumVimType.K8S);
        mepHost.setMecHostUserName("test");
        mepHost.setMecHostPassword("test");
        mepHost.setMecHostPort(20000);
        mepHost.setUserId(UUID.randomUUID().toString());
        mepHost.setConfigId(UUID.randomUUID().toString());
        mepHost.setNetworkParameter("net param");
        mepHost.setResource("resource");
        mepHost.setAddress("xi'an");
        return mepHost;
    }

    private MepHost createAnotherHost() {
        MepHost mepHost = new MepHost();
        mepHost.setId(UUID.randomUUID().toString());
        mepHost.setName("openstack-test");
        mepHost.setLcmIp("1.1.1.10");
        mepHost.setLcmProtocol("https");
        mepHost.setLcmPort(30100);
        mepHost.setArchitecture("X86");
        mepHost.setStatus(EnumMepHostStatus.NORMAL);
        mepHost.setMecHostIp("1.1.1.10");
        mepHost.setVimType(EnumVimType.OpenStack);
        mepHost.setMecHostUserName("test");
        mepHost.setMecHostPassword("test");
        mepHost.setMecHostPort(20000);
        mepHost.setUserId(UUID.randomUUID().toString());
        mepHost.setConfigId(UUID.randomUUID().toString());
        mepHost.setNetworkParameter("a:b;test:1we");
        mepHost.setResource("resource");
        mepHost.setAddress("xi'an");
        return mepHost;
    }

    private MepHost createHost() {
        MepHost mepHost = new MepHost();
        mepHost.setId(UUID.randomUUID().toString());
        mepHost.setName("k8s-test-01");
        mepHost.setLcmIp("localhost");
        mepHost.setLcmProtocol("http");
        mepHost.setLcmPort(31252);
        mepHost.setArchitecture("X86");
        mepHost.setStatus(EnumMepHostStatus.NORMAL);
        mepHost.setMecHostIp("1.1.1.2");
        mepHost.setVimType(EnumVimType.K8S);
        mepHost.setMecHostUserName("test");
        mepHost.setMecHostPassword("test");
        mepHost.setMecHostPort(20000);
        mepHost.setUserId(UUID.randomUUID().toString());
        mepHost.setConfigId("");
        mepHost.setNetworkParameter("test");
        mepHost.setResource("resource");
        mepHost.setAddress("xi'an");
        return mepHost;
    }

}
