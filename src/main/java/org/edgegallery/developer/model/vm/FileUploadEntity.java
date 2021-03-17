package org.edgegallery.developer.model.vm;

import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileUploadEntity {
    private String code;

    private String message;

    private File file;

    public FileUploadEntity() {
    }

    /**
     * FileUploadEntity.
     */
    public FileUploadEntity(String code, String message, File file) {
        super();
        this.code = code;
        this.message = message;
        this.file = file;
    }

}
