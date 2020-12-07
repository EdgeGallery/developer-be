package org.edgegallery.developer.util;

import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.service.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AppStoreUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilsService.class);

    private static final String APPSTORE_ADDRESS = "appstore.address";

    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * upload app to appstore.
     */
    public static ResponseEntity<String> storeToAppStore(Map<String, Object> params, String userId, String userName,
        String token) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        params.forEach(map::add);

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = String
            .format("%s/mec/appstore/v1/apps?userId=%s&userName=%s", InitConfigUtil.getProperties(APPSTORE_ADDRESS),
                userId, userName);

        try {
            ResponseEntity<String> responses = restTemplate
                .exchange(url, HttpMethod.POST, new HttpEntity<>(map, headers), String.class);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses;
            }
            LOGGER.error("Upload appstore failed,  status is {}", responses.getStatusCode());
        } catch (InvocationException e) {
            LOGGER.error("Failed to upload appstore,  exception {}", e.getMessage());

        }
        throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR, "Upload appstore failed.");
    }

    /**
     * publish app to appstore.
     */
    public static ResponseEntity<String> publishToAppStore(String appId, String pkgId,String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = String.format("%s/mec/appstore/v1/app/%s/packages/%s/action/publish",
            InitConfigUtil.getProperties(APPSTORE_ADDRESS), appId, pkgId);

        try {
            ResponseEntity<String> responses = restTemplate
                .exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses;
            }
            LOGGER.error("publish app failed,  status is {}", responses.getStatusCode());
        } catch (InvocationException e) {
            LOGGER.error("publish app  failed,  exception {}", e.getMessage());

        }
        throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR, "publish app failed.");
    }

}
