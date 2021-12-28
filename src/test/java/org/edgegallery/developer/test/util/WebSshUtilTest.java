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
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.HttpClientUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class WebSshUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtilTest.class);

    @Before
    public void setUp() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("", 8888), 10);
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    String jsonStr = null;
                    try {
                        File file = Resources.getResourceAsFile("testdata/json/ssh_response_response.json");
                        jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                    } catch (IOException e) {
                        LOGGER.error("Load the mock json data for getDistributeRes failed.");
                    }
                    byte[] response = jsonStr.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.start();
    }

    @Test
    public void testSendWebSshRequestSuccess() {
        SshResponseInfo result = HttpClientUtil
            .sendWebSshRequest("http://127.0.0.1:8888/", "127.0.0.1", 8080, "username", "password", "");
        Assert.assertNull(result);
    }


}
