package org.edgegallery.developer.model.restful;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.edgegallery.developer.model.application.ContainerApplication;
import org.edgegallery.developer.model.application.VMApplication;

@ToString
@Getter
@Setter
public class ApplicationDetail {

    private VMApplication vmApp;

    private ContainerApplication ContainerApp;


}
