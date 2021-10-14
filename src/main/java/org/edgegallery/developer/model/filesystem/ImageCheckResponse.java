package org.edgegallery.developer.model.filesystem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageCheckResponse {
    private int status;

    private String msg;

    private CheckInfo checkInfo;

}
