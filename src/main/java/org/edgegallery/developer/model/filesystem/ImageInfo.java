package org.edgegallery.developer.model.filesystem;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class ImageInfo {

    @JsonProperty("image-end-offset")
    private String imageSize;

    @JsonProperty("check-errors")
    private String checkErrors;

    private String format;

    @JsonProperty("filename")
    private String fileName;

}
