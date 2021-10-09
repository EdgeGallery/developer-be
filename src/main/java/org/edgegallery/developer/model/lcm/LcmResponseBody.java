package org.edgegallery.developer.model.lcm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LcmResponseBody {

    private Object data;

    private String retCode;

    private String message;

    private String params;

}
