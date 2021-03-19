package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VmImageInfo {

    private String imageId;

    private String imageName;

    private String appInstanceId;

    private String status;

    private Integer sumChunkNum;

    private Integer chunkSize;


}
