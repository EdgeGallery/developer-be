package org.edgegallery.developer.model.releasedpackage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AppPkgFile {

    private String fileName;

    private String filePath;

    private boolean isFile;

    private List<AppPkgFile> children;

    private transient String content;
}
