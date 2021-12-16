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

package org.edgegallery.developer.test.service.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.HttpClientUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ReverseProxyServiceTest {

    @Autowired
    private ReverseProxyService proxyService;

    private HttpServer httpServer;

    @Before
    public void setUp() throws IOException {

        httpServer = HttpServer.create(new InetSocketAddress("localhost", 30101), 0);
        httpServer.createContext("/commonservice/cbb/v1/reverseproxies", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    String res = "ok";
                    byte[] response = res.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/commonservice/cbb/v1/reverseproxies/dest-host-ip/1.1.1.3/dest-host-port/20000",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("DELETE")) {
                        String res = "ok";
                        byte[] response = res.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext("/commonservice/cbb/v1/reverseproxies/dest-host-ip/1.1.1.3/dest-host-port/6080",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("GET")) {
                        String res
                            = "{\"destHostIp\":\"1.1.13\",\"destHostPort\":30000,\"localPort\":20000,\"nextHopProtocol\":\"http\",\"nextHopIp\":\"1.1.1.3\",\"nextHopPort\":20004,\"hopIndex\":3}";
                        byte[] response = res.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
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
    public void testAddReverseProxySuccess() {
        String hostId = "fe934a92-1cfc-42fe-919d-422e2e3bd1f9";
        int hostConsolePort = 20000;
        proxyService.addReverseProxy(hostId, hostConsolePort, "");
    }

    @Test
    public void testDeleteReverseProxySuccess() {
        String hostId = "fe934a92-1cfc-42fe-919d-422e2e3bd1f8";
        int hostConsolePort = 20000;
        proxyService.deleteReverseProxy(hostId, hostConsolePort, "");
    }

    @Test
    public void testGetVmConsoleUrlFail1() {
        try {
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7b";
            proxyService.getVmConsoleUrl(applicationId, "", "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("failed to get vnc console url", e.getMessage());
        }
    }

    @Test
    public void testGetVmConsoleUrlFail2() {
        new MockUp<HttpClientUtil>() {
            @Mock
            public String getWorkloadStatus(String basePath, String appInstanceId, String userId, String token) {
                return null;
            }
        };
        try {
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7b";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
            proxyService.getVmConsoleUrl(applicationId, vmId, "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("failed to get vnc console url", e.getMessage());
        }
    }

    @Test
    public void testGetVmConsoleUrlFail3() {
        new MockUp<HttpClientUtil>() {
            @Mock
            public String getWorkloadStatus(String basePath, String appInstanceId, String userId, String token) {
                return "{\"code\":\"200\",\"msg\":\"success\",\"status\":\"ok\",\"data\":[]}";
            }
        };
        try {
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7b";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
            proxyService.getVmConsoleUrl(applicationId, vmId, "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("failed to get vnc console url", e.getMessage());
        }
    }

    @Test
    public void testGetVmConsoleUrlFail4() {
        new MockUp<HttpClientUtil>() {
            @Mock
            public String getWorkloadStatus(String basePath, String appInstanceId, String userId, String token) {
                return "{\"code\":\"200\",\"msg\":\"success\",\"status\":\"ok\",\"data\":[{\"vmId\":\"vmId\",\"vncurl\":\"localhost:30101\",\"networks\":[]}]}";
            }
        };
        try {
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7b";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
            proxyService.getVmConsoleUrl(applicationId, vmId, "", "");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetVmSshResponseInfoFail1() {
        try {
            // String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7b";
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d757";
            proxyService.getVmSshResponseInfo(applicationId, vmId, "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("failed to get ssh console url", e.getMessage());
        }
    }

    @Test
    public void testGetVmSshResponseInfoFail2() {
        try {
            new MockUp<HttpClientUtil>() {
                @Mock
                public SshResponseInfo sendWebSshRequest(String basePath, String hostIp, int port, String username,
                    String password, String XSRFValue) {
                    return null;
                }
            };
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
            proxyService.getVmSshResponseInfo(applicationId, vmId, "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("failed to get ssh console url", e.getMessage());
        }
    }

    @Test
    public void testGetVmSshResponseInfoFail3() {
        try {
            new MockUp<HttpClientUtil>() {
                @Mock
                public SshResponseInfo sendWebSshRequest(String basePath, String hostIp, int port, String username,
                    String password, String XSRFValue) {
                    SshResponseInfo sshResponseInfo = new SshResponseInfo();
                    sshResponseInfo.setId("");
                    return sshResponseInfo;
                }
            };
            String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";
            String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
            proxyService.getVmSshResponseInfo(applicationId, vmId, "", "");
        } catch (DeveloperException e) {
            Assert.assertEquals("WebSsh info input error", e.getMessage());
        }
    }

    @Test
    public void testGetVmSshResponseInfoSuccess() {
        new MockUp<HttpClientUtil>() {
            @Mock
            public SshResponseInfo sendWebSshRequest(String basePath, String hostIp, int port, String username,
                String password, String XSRFValue) {
                SshResponseInfo sshResponseInfo = new SshResponseInfo();
                sshResponseInfo.setId(UUID.randomUUID().toString());
                sshResponseInfo.setEncoding("utf8");
                sshResponseInfo.setStatus("success");
                return sshResponseInfo;
            }
        };
        String applicationId = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";
        String vmId = "6a75a2bd-9811-432f-bbe8-2813aa97d758";
        Assert.assertNotNull(proxyService.getVmSshResponseInfo(applicationId, vmId, "", ""));
    }

}
