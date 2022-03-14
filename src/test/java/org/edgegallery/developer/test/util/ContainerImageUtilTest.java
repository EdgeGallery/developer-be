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
import mockit.Mock;
import mockit.MockUp;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.util.ContainerImageUtil;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContainerImageUtilTest {

    private HttpServer httpServer;

    private MockUp mockup;

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
        httpServer.createContext("/api/v2.0/projects?name=test", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    String res = "null";
                    byte[] response = res.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/api/v2.0/projects", new HttpHandler() {
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
        httpServer
            .createContext("/api/v2.0/projects/projectName/repositories/name/artifacts/version", new HttpHandler() {
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

        httpServer.createContext("/api/v2.0/projects/test/repositories/?page=1&page_size=1000", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    String res
                        = "[{\"artifact_count\":10,\"creation_time\":\"xxxxxx\",\"id\":\"id\",\"name\":\"name\",\"project_id\":\"xxxxxxxxxxxx\",\"pull_count\":5,\"update_time\":\"xxxxxxx\"}]";
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
    public void testEncodeUserAndPwd() {
        Assert.assertNotNull(ContainerImageUtil.encodeUserAndPwd("admin", "123456"));
    }

    @Test
    public void testDeCompressTar() throws IOException {
        File rootDir = Resources.getResourceAsFile("testdata/nginx_package");
        File imageTar = Resources.getResourceAsFile("testdata/nginx.tar");
        boolean ret = ContainerImageUtil.deCompressTar(imageTar.getCanonicalPath(), rootDir);
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testGetDockerClient() throws IOException {
        try {
            ContainerImageUtil.getDockerClient();
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCreateIgnoreSslHttpClient() throws IOException {
        Assert.assertNotNull(ContainerImageUtil.createIgnoreSslHttpClient());
    }

    @Test
    public void testIsExist() {
        try {
            ContainerImageUtil.isExist("test");
        }catch (Exception e){
            Assert.assertEquals("call get one project occur error!", e.getMessage());
        }

    }

    @Test
    public void testCreateHarborRepo() throws IOException {
        Assert.assertEquals(ContainerImageUtil.createHarborRepo("test123"), false);
    }

    @Test
    public void testDeCompressAndGetRePoTags() throws IOException {
        File rootDir = Resources.getResourceAsFile("testdata/nginx_package");
        File imageTar = Resources.getResourceAsFile("testdata/nginx.tar");
        String tag = ContainerImageUtil.deCompressAndGetRePoTags(rootDir.getCanonicalPath() + File.separator, imageTar);
        Assert.assertNotNull(tag);
    }

    @Test
    public void testRetagAndPush() throws IOException {
        try {
            ContainerImageUtil.reTagAndPush(ContainerImageUtil.getDockerClient(), "aa", "test", "nginx:latest");
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetImageIdFromRepoTags() throws IOException {
        try {
            ContainerImageUtil.getImageIdFromRepoTags("nginx:latest", ContainerImageUtil.getDockerClient());
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeleteImage() throws IOException {
        AccessUserUtil.setUser("userId", "admin");
        boolean ret = ContainerImageUtil.deleteImage("1.1.1.1/test/nginx:latest", "admin");
        Assert.assertEquals(ret, false);
    }

    @Test
    public void testGetHarborImageList() throws IOException {
        try {
            ContainerImageUtil.getHarborImageList();
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

}
