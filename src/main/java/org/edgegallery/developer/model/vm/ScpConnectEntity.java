package org.edgegallery.developer.model.vm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScpConnectEntity {

    private String userName;

    private String passWord;

    private String url;

    private String targetPath;

}
