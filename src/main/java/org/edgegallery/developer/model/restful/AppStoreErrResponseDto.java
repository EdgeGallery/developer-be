package org.edgegallery.developer.model.restful;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppStoreErrResponseDto {

    private String timestamp;

    private int code;

    private String error;

    private String message;

    private String path;

    private int retCode;

    private List<String> params;

    private Object data;
}
