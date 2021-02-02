package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Resource {

    private Limits limits;
    private Requests requests;

}
