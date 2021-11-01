package org.edgegallery.developer.model.restful;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class VncUrlRep {
    private String vncUrl;

    public VncUrlRep(String vncUrl) {
        this.vncUrl = vncUrl;
    }
}
