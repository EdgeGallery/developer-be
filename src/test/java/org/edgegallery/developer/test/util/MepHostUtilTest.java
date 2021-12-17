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
import javax.annotation.Resource;
import mockit.Mock;
import mockit.MockUp;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.MepHostUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MepHostUtilTest {

    private HttpServer httpServer;

    @Before
    public void setUp() throws IOException {
        new MockUp<InitConfigUtil>() {
            @Mock
            public String getWorkSpaceBaseDir() {
                return "";
            }
        };

        httpServer = HttpServer.create(new InetSocketAddress("localhost", 31252), 0);
        httpServer.createContext("/lcmcontroller/v1/tenants/userId/hosts", new HttpHandler() {
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
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/configuration", new HttpHandler() {
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
    public void testAddMecHostToLcmSuccess() throws IOException {
        MepHost mepHost = new MepHost();
        mepHost.setArchitecture("X86");
        mepHost.setAddress("xian");
        mepHost.setMecHostIp("localhost");
        mepHost.setName("k8s-test");
        mepHost.setVimType(EnumVimType.K8S);
        mepHost.setLcmProtocol("http");
        mepHost.setLcmIp("localhost");
        mepHost.setLcmPort(31252);
        mepHost.setUserId("userId");
        boolean ret = MepHostUtil.addMecHostToLcm(mepHost, "token");
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testAddMecHostToLcmSuccess1() throws IOException {
        MepHost mepHost = new MepHost();
        mepHost.setArchitecture("X86");
        mepHost.setAddress("xian");
        mepHost.setMecHostIp("localhost");
        mepHost.setName("op1-test");
        mepHost.setVimType(EnumVimType.OpenStack);
        mepHost.setLcmProtocol("http");
        mepHost.setLcmIp("localhost");
        mepHost.setLcmPort(31252);
        mepHost.setUserId("userId");
        boolean ret = MepHostUtil.addMecHostToLcm(mepHost, "token");
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testAddMecHostToLcmSuccess2() throws IOException {
        MepHost mepHost = new MepHost();
        mepHost.setArchitecture("X86");
        mepHost.setAddress("xian");
        mepHost.setMecHostIp("localhost");
        mepHost.setName("fu1-test");
        mepHost.setVimType(EnumVimType.FusionSphere);
        mepHost.setLcmProtocol("http");
        mepHost.setLcmIp("localhost");
        mepHost.setLcmPort(31252);
        mepHost.setUserId("userId");
        boolean ret = MepHostUtil.addMecHostToLcm(mepHost, "token");
        Assert.assertEquals(true, ret);
    }

    @Test
    public void tesUploadFileToLcmSuccess() throws IOException {
        MepHost mepHost = new MepHost();
        mepHost.setArchitecture("X86");
        mepHost.setAddress("xian");
        mepHost.setMecHostIp("localhost");
        mepHost.setName("fu1-test");
        mepHost.setVimType(EnumVimType.FusionSphere);
        mepHost.setLcmProtocol("http");
        mepHost.setLcmIp("localhost");
        mepHost.setLcmPort(31252);
        mepHost.setUserId("userId");
        File file = Resources.getResourceAsFile("testdata/config");
        boolean ret = MepHostUtil.uploadFileToLcm(mepHost, file.getCanonicalPath(),"token");
        Assert.assertEquals(true, ret);
    }

}
