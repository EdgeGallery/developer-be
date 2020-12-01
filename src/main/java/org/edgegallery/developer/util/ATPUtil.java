package org.edgegallery.developer.util;

import java.io.File;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.common.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * ATP Util
 */
public class ATPUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ATPUtil.class);

    private static final String WAITING = "waiting";

    private static final String RUNNING = "running";

    private static final String ATP_ADDRESS = "atp_address";

    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * send request to atp to create test task.
     * 
     * @param filePath csar file path
     * @param token request token
     * @return response from atp
     */
    public static ResponseEntity<String> sendCreatTask2ATP(String filePath, String token) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = InitConfigUtil.getProperties(ATP_ADDRESS).concat(Consts.CREATE_TASK_FROM_ATP);
        LOGGER.info("url: {}", url);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())
                    || HttpStatus.ACCEPTED.equals(response.getStatusCode())) {
                return response;
            }
            LOGGER.error("Create instance from atp failed,  status is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to create instance from atp,  exception {}", e.getMessage());
        }

        throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR, "Create instance from atp failed.");
    }

    /**
     * get task status by taskId from atp
     * 
     * @param taskId taskId
     * @param token token
     * @return task status
     */
    public static String getTaskStatusFromATP(String taskId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = InitConfigUtil.getProperties(ATP_ADDRESS).concat(String.format(Consts.GET_TASK_FROM_ATP, taskId));
        LOGGER.info("get task status frm atp, url: {}", url);

        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
                if (!HttpStatus.OK.equals(response.getStatusCode())) {
                    LOGGER.error("Get task status from atp reponse failed, the taskId is {}, The status code is {}",
                            taskId, response.getStatusCode());
                    throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR,
                            "Get task status from atp reponse failed.");
                }

                JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
                String status = jsonObject.get("status").getAsString();

                LOGGER.info("status: {}", status);

                if (!WAITING.equalsIgnoreCase(status) && !RUNNING.equals(status)) {
                    return status;
                }

                if ((System.currentTimeMillis() - startTime) > 30000) {
                    LOGGER.error("Get atp task {} status from appo time out", taskId);
                    throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR,
                            "Get atp task status from appo time out.");
                }
                Thread.sleep(5000);
            } catch (RestClientException e) {
                LOGGER.error("Failed to get task status from atp which taskId is {} exception {}", taskId,
                        e.getMessage());
            } catch (InterruptedException e) {
                LOGGER.error("thead sleep exception.");
            }
        }
    }

    /**
     * get file path by projectId
     * 
     * @param projectId projectId
     * @return filePath
     */
    public static String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
                + File.separator;
    }
}