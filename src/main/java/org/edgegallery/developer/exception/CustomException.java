package org.edgegallery.developer.exception;

import org.springframework.web.client.RestClientException;

public class CustomException extends RestClientException {

    private RestClientException restClientException;

    private String body;

    public RestClientException getRestClientException() {
        return restClientException;
    }

    public void setRestClientException(RestClientException restClientException) {
        this.restClientException = restClientException;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * cus.
     *
     * @param msg msg
     * @param restClientException rest
     * @param body body
     */
    public CustomException(String msg, RestClientException restClientException, String body) {
        super(msg);
        this.restClientException = restClientException;
        this.body = body;
    }

}
