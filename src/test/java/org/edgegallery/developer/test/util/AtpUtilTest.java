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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import mockit.Mock;
import mockit.MockUp;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.AtpUtil;
import org.edgegallery.developer.util.RestSvcAddressConfig;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AtpUtilTest {

    private HttpServer httpServer;

    MockUp mockup;

    @Before
    public void setUp() throws IOException {
        mockup = new MockUp<SpringContextUtil>() {
            @Mock
            public Object getBean(Class<?> requiredType) {
                RestSvcAddressConfig restSvcAddressConfig = new RestSvcAddressConfig();
                restSvcAddressConfig.setAtpAddress("http://localhost:8073");
                return restSvcAddressConfig;
            }
        };

        httpServer = HttpServer.create(new InetSocketAddress("", 8073), 0);
        httpServer.createContext("/edgegallery/atp/v1/tasks", new HttpHandler() {
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
        httpServer.createContext("/edgegallery/atp/v1/tasks/taskId", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    String res = "{\"status\": \"success\" }";
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
        mockup.tearDown();
    }

    @Test
    public void testSendCreateTask2AtpSuccess() throws IOException {
        File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        String result = AtpUtil.sendCreateTask2Atp(file.getCanonicalPath(), "");
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testGetTaskStatusFromAtpSuccess() {
        String result = AtpUtil.getTaskStatusFromAtp("taskId");
        Assert.assertEquals("success", result);
    }

}
