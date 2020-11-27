package org.edgegallery.developer.model;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class AppPkgStructure {
    private String name;

    private String id;

    private List<AppPkgStructure> children;

    private boolean isParent = true;

    public boolean isParent() {
        return isParent;
    }

}
