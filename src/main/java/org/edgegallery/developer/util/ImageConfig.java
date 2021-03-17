package org.edgegallery.developer.util;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageConfig {
    @Value("${imagelocation.domainname}")
    private String domainname;

    @Value("${imagelocation.project}")
    private String project;

    private static String domain;
    private static String pro;

    @Value("${user.name}")
    private String name;

    /**
     * getApiToken.
     */
    @PostConstruct
    public void getApiToken() {
        domain = this.domainname;
        pro = this.project;

    }

    public static String getDomains() {
        return domain;
    }

    public static String getProjects() {
        return pro;
    }
}
