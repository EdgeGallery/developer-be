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
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContainerAppHelmChartUtilTest {
    private HttpServer httpServer;

    MockUp mockup;

    @Before
    public void setUp() throws IOException {
        mockup = new MockUp<SpringContextUtil>() {
            @Mock
            public Object getBean(Class<?> requiredType) {
                ImageConfig imageConfig = new ImageConfig();
                imageConfig.setDomainname("1.1.1.1");
                imageConfig.setUsername("test");
                imageConfig.setPassword("123456");
                imageConfig.setProject("test");
                return imageConfig;
            }
        };

        httpServer = HttpServer.create(new InetSocketAddress("", 80), 0);
        httpServer
            .createContext("/api/v2.0/projects/test/repositories/name/artifacts/version", new HttpHandler() {
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
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
        mockup.tearDown();
    }

    @Test
    public void testCheckImageExistFail1() throws IOException {
        List<String> imageList = new ArrayList<>();
        Assert.assertNull(ContainerAppHelmChartUtil.getImageCheckInfo(imageList));
    }

    @Test
    public void testCheckImageExistFail2() throws IOException {
        List<String> imageList = new ArrayList<>();
        imageList.add("test");
        String res = ContainerAppHelmChartUtil.getImageCheckInfo(imageList);
        Assert.assertEquals("image test is not in standard format",res);
    }

    @Test
    public void testCheckImageExistFail3() throws IOException {
        List<String> imageList = new ArrayList<>();
        imageList.add("test:");
        String res = ContainerAppHelmChartUtil.getImageCheckInfo(imageList);
        Assert.assertEquals("image test: is not in standard format",res);
    }

    @Test
    public void testCheckImageExistFail4() throws IOException {
        List<String> imageList = new ArrayList<>();
        imageList.add("test:1");
        String res = ContainerAppHelmChartUtil.getImageCheckInfo(imageList);
        Assert.assertEquals("image test:1 is not in standard format",res);
    }

    @Test
    public void testCheckImageExistFail5() throws IOException {
        List<String> imageList = new ArrayList<>();
        imageList.add("1.1.1.1/test/name:version");
        String res = ContainerAppHelmChartUtil.getImageCheckInfo(imageList);
        Assert.assertEquals("image 1.1.1.1/test/name:version does not exist in harbor repo!",res);
    }

    @Test
    public void testCheckFileNameFormatFail() throws IOException {
        File tgzFile = Resources.getResourceAsFile("testdata/helmcharts/aixiaochu1.tgz");
        boolean res = ContainerAppHelmChartUtil.checkFileNameFormat(tgzFile.getCanonicalPath());
        Assert.assertFalse(res);
    }

    @Test
    public void testCheckFileNameFormatSuccess() throws IOException {
        File tgzFile = Resources.getResourceAsFile("testdata/helmcharts/2048.tgz");
        boolean res = ContainerAppHelmChartUtil.checkFileNameFormat(tgzFile.getCanonicalPath());
        Assert.assertTrue(res);
    }
}
