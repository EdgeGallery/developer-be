package org.edgegallery.developer.test.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.test.service.uploadfile.UploadServiceTest;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.AtpUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AtpUtilTest {

    private HttpServer httpServer;

    @Before
    public void setUp() throws IOException {
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
                    String res ="{\"status\": \"success\" }";
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
    public void testSendCreatTask2AtpSuccess() throws IOException {
        File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        String result = AtpUtil.sendCreatTask2Atp(file.getCanonicalPath(), "");
        Assert.assertEquals("ok", result);
    }

    @Test
    public void testGetTaskStatusFromAtpSuccess() {
        String result = AtpUtil.getTaskStatusFromAtp("taskId");
        Assert.assertEquals("success", result);
    }

}
