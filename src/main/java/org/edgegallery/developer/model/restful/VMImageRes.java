package org.edgegallery.developer.model.restful;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.developer.model.vmimage.VMImage;
@Getter
@Setter
public class VMImageRes {
    private int totalCount;

    private List<VMImage> imageList;

}
