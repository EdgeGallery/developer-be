package org.edgegallery.developer.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class RestSvcAddressConfig {

    @Value("${rest.appstore}")
    private String appstoreAddress;

    @Value("${rest.atp}")
    private String atpAddress;

}
