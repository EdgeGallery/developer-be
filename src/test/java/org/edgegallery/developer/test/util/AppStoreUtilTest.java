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

package org.edgegallery.developer.test.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.AppStoreUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AppStoreUtilTest {

    private HttpServer httpServer;

    @Before
    public void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("", 8099), 0);
        httpServer.createContext("/mec/appstore/v1/apps", new HttpHandler() {
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
        httpServer.createContext("/mec/appstore/v2/apps/appId/packages/packageId", new HttpHandler() {
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
        httpServer.createContext("/mec/appstore/v1/apps/appId/packages/packageId/action/download", new HttpHandler() {
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
        httpServer
            .createContext("/mec/appstore/v1/apps/applicationId/packages/packageId/action/publish", new HttpHandler() {
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
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    public void testStoreToAppStoreSuccess() {
        User user = new User("testId", "testUser", "testAuth", "testToken");
        Map<String, Object> map = new HashMap<>();
        String result = AppStoreUtil.storeToAppStore(map, user);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testPublishToAppStoreSuccess() {
        PublishAppReqDto pubAppReqDto = new PublishAppReqDto();
        pubAppReqDto.setFree(false);
        pubAppReqDto.setPrice(10);
        String result = AppStoreUtil.publishToAppStore("applicationId", "packageId", "", pubAppReqDto);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testGetPkgInfoSuccess() {
        String ret = AppStoreUtil.getPkgInfo("appId", "packageId", "");
        Assert.assertNotNull(ret);
    }

    @Test
    public void testDownloadPkgSuccess() {
        byte[] ret = AppStoreUtil.downloadPkg("appId", "packageId", "");
        Assert.assertNotNull(ret);
    }

}
