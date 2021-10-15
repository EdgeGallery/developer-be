package org.edgegallery.developer.model.filesystem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSystemResponse {

    private String imageId;

    private String fileName;

    private String uploadTime;

    private String storageMedium;

    private int slimStatus;

    private String userId;

    private ImageCheckResponse checkStatusResponse;


}
