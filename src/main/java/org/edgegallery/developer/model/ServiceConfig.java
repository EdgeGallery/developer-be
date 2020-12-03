package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author chenhui
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceConfig {

    /**
     * the name for service.
     * example: position-service
     */
    private String name;

    /**
     * the port of service
     * example: 9999
     */
    private int port;

    /**
     * version
     * example: 1.0
     */
    private String version;


    /**
     * protocol
     * example: HTTP or HTTPS
     */
    private String protocol;

}
