package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodStatusInfo {

    private String podstatus;

    private String podname;

    private String[] podEventsInfo;

   private PodContainers[] containers;
}
