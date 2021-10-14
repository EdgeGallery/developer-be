package org.edgegallery.developer.model.filesystem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInfo {

    private String checksum;

    private int checkResult;

    private ImageInfo imageInfo;

}
