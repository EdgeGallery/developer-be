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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.HttpClientUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class HttpClientUtilTest {

    public static final String LCM_URL = "http://127.0.0.1:30204";

    public static final String APPLICATION_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    public static final String USER_ID = "5ce78873-d73d-4e7d-84a4-ab75ac95400f";

    public static final String TOKEN = "token";

    public static final LcmLog LCM_LOG = new LcmLog();

    public static final String PACKAGE_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    public static final String MEC_HOST = "1.1.1.1";

    public static final String VM_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    public static final String IMAGE_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    public static final Map<String, String> INPUT_PARAMS = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtilTest.class);

    private HttpServer httpServer;

    @Before
    public void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("", 30204), 10);
        httpServer.createContext(String.format(Consts.APP_LCM_INSTANTIATE_APP_URL, USER_ID, APPLICATION_ID),
            new HttpHandler() {
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
        httpServer.createContext(String.format(Consts.APP_LCM_UPLOAD_APPPKG_URL, USER_ID), new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/package_upload.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }
                if (method.equals("POST")) {

                    byte[] response = jsonStr.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                if (method.equals("GET")) {

                    byte[] response = jsonStr.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer
            .createContext(String.format(Consts.APP_LCM_DISTRIBUTE_APPPKG_URL, USER_ID, PACKAGE_ID), new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("POST")) {
                        String res = "ok";
                        byte[] response = res.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    if (method.equals("GET")) {
                        String jsonStr = null;
                        try {
                            File file = Resources.getResourceAsFile("testdata/json/package_distribute_status.json");
                            jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                        } catch (IOException e) {
                            LOGGER.error("Load the mock json data for getDistributeRes failed.");
                        }
                        byte[] response = jsonStr.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    if (method.equals("DELETE")) {

                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext(String.format(Consts.APP_LCM_DELETE_HOST_URL, USER_ID, PACKAGE_ID, MEC_HOST),
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("DELETE")) {

                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });
        httpServer
            .createContext(String.format(Consts.APP_LCM_TERMINATE_APP_URL, USER_ID, APPLICATION_ID), new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("POST")) {

                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext(String.format(Consts.APP_LCM_GET_WORKLOAD_STATUS_URL, USER_ID, APPLICATION_ID),
            new HttpHandler() {
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

        httpServer.createContext(String.format(Consts.APP_LCM_GET_WORKLOAD_EVENTS_URL, USER_ID, APPLICATION_ID),
            new HttpHandler() {
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

        httpServer.createContext(Consts.APP_LCM_GET_HEALTH, new HttpHandler() {
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

        httpServer.createContext(String.format(Consts.APP_LCM_INSTANTIATE_IMAGE_URL, USER_ID, MEC_HOST, VM_ID),
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("POST")) {
                        String jsonStr = null;
                        try {
                            File file = Resources.getResourceAsFile("testdata/json/package_upload.json");
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

        httpServer.createContext(String.format(Consts.APP_LCM_GET_IMAGE_STATUS_URL, USER_ID, MEC_HOST, IMAGE_ID),
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("GET")) {
                        String jsonStr = null;
                        try {
                            File file = Resources.getResourceAsFile("testdata/json/package_upload.json");
                            jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                        } catch (IOException e) {
                            LOGGER.error("Load the mock json data for getDistributeRes failed.");
                        }
                        byte[] response = jsonStr.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    if (method.equals("DELETE")) {

                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });

        httpServer.createContext(Consts.SYSTEM_IMAGE_SLICE_UPLOAD_URL, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                if (method.equals("DELETE")) {

                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });

        httpServer.createContext(Consts.SYSTEM_IMAGE_SLICE_MERGE_URL, new HttpHandler() {
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

        httpServer.createContext(String.format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, IMAGE_ID), new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {

                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });

        httpServer.createContext(String.format(Consts.SYSTEM_IMAGE_GET_URL, IMAGE_ID) + "/slim", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {

                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });

        httpServer.createContext(String.format(Consts.SYSTEM_IMAGE_GET_URL, IMAGE_ID), new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    String jsonStr = null;
                    try {
                        File file = Resources.getResourceAsFile("testdata/json/file_system_response.json");
                        jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                    } catch (IOException e) {
                        LOGGER.error("Load the mock json data for getDistributeRes failed.");
                    }
                    byte[] response = jsonStr.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                if (method.equals("DELETE")) {

                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }

                exchange.close();
            }
        });

        httpServer.createContext(String.format(Consts.APP_LCM_GET_VNC_CONSOLE_URL, USER_ID, MEC_HOST, VM_ID),
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    if (method.equals("POST")) {
                        String jsonStr = null;
                        try {
                            File file = Resources.getResourceAsFile("testdata/json/package_upload.json");
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

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    public void testInstantiateApplicationSuccess() {
        boolean result = HttpClientUtil
            .instantiateApplication(LCM_URL, APPLICATION_ID, USER_ID, TOKEN, LCM_LOG, PACKAGE_ID, MEC_HOST,
                INPUT_PARAMS);
        Assert.assertTrue(result);
    }

    @Test
    public void testUploadPkgSuccess() throws IOException {
        File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        String result = HttpClientUtil.uploadPkg(LCM_URL, file.getCanonicalPath(), USER_ID, TOKEN, LCM_LOG);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testDistributePkgSuccess() {
        String result = HttpClientUtil.distributePkg(LCM_URL, USER_ID, TOKEN, PACKAGE_ID, MEC_HOST, LCM_LOG);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testDeleteHostSuccess() {
        boolean result = HttpClientUtil.deleteHost(LCM_URL, USER_ID, TOKEN, PACKAGE_ID, MEC_HOST);
        Assert.assertTrue(result);
    }

    @Test
    public void testDeletePkgSuccess() {
        boolean result = HttpClientUtil.deletePkg(LCM_URL, USER_ID, TOKEN, PACKAGE_ID);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetDistributeResSuccess() {
        String result = HttpClientUtil.getDistributeRes(LCM_URL, USER_ID, TOKEN, PACKAGE_ID, LCM_LOG);
        Assert.assertNotNull(result);
    }

    @Test
    public void testTerminateAppInstanceSuccess() {
        boolean result = HttpClientUtil.terminateAppInstance(LCM_URL, APPLICATION_ID, USER_ID, TOKEN);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetWorkloadStatusSuccess() {
        String result = HttpClientUtil.getWorkloadStatus(LCM_URL, APPLICATION_ID, USER_ID, TOKEN, LCM_LOG);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testGetWorkloadEventsSuccess() {
        String result = HttpClientUtil.getWorkloadEvents(LCM_URL, APPLICATION_ID, USER_ID, TOKEN, LCM_LOG);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testGetHealthSuccess() {
        String result = HttpClientUtil.getHealth(LCM_URL);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testVmInstantiateImageSuccess() {
        String result = HttpClientUtil.vmInstantiateImage(LCM_URL, USER_ID, TOKEN, VM_ID, MEC_HOST, "", LCM_LOG);
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetImageStatusImageSuccess() {
        String result = HttpClientUtil.getImageStatus(LCM_URL, MEC_HOST, USER_ID, IMAGE_ID, TOKEN);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDeleteVmImageSuccess() {
        boolean result = HttpClientUtil.deleteVmImage(LCM_URL, USER_ID, MEC_HOST, IMAGE_ID, TOKEN);
        Assert.assertTrue(result);
    }

    @Test
    public void testSliceUploadFileSuccess() throws IOException {
        File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        Chunk chunk = new Chunk();
        chunk.setFile(null);
        chunk.setChunkNumber(4);
        chunk.setCurrentChunkSize(24173056L);
        chunk.setTotalSize(24173056L);
        chunk.setFilename("nginx.tar");
        chunk.setRelativePath("nginx.tar");
        chunk.setTotalChunks(3);
        boolean result = HttpClientUtil.sliceUploadFile(LCM_URL, chunk, file.getCanonicalPath());
        Assert.assertTrue(result);
    }

    @Test
    public void testCancelSliceUploadSuccess() {
        boolean result = HttpClientUtil.cancelSliceUpload(LCM_URL, "123");
        Assert.assertTrue(result);
    }

    @Test
    public void testSliceMergeFileSuccess() {
        String result = HttpClientUtil.sliceMergeFile(LCM_URL, "123", "123", USER_ID);
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testDeleteSystemImageSuccess() {
        boolean result = HttpClientUtil
            .deleteSystemImage(LCM_URL + String.format(Consts.SYSTEM_IMAGE_GET_URL, IMAGE_ID));
        Assert.assertTrue(result);
    }

    @Test
    public void testDownloadSystemImageSuccess() {
        byte[] data = HttpClientUtil
            .downloadSystemImage(LCM_URL + String.format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, IMAGE_ID));
        Assert.assertNull(data);
    }

    @Test
    public void testImageSlimSuccess() {
        boolean result = HttpClientUtil
            .imageSlim(LCM_URL + String.format(Consts.SYSTEM_IMAGE_GET_URL, IMAGE_ID) + "/slim");
        Assert.assertTrue(result);
    }

    @Test
    public void testQueryImageCheckSuccess() {
        FileSystemResponse result = HttpClientUtil
            .queryImageCheck(LCM_URL + String.format(Consts.SYSTEM_IMAGE_GET_URL, IMAGE_ID));
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetVncUrlSuccess() {
        String result = HttpClientUtil.getVncUrl(LCM_URL, USER_ID, MEC_HOST, VM_ID, "");
        Assert.assertNotNull(result);
    }

}
