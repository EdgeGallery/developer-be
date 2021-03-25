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

    /**
     * getPodEventsInfo.
     *
     * @return
     */
    public String[] getPodEventsInfo() {
        if (podEventsInfo != null) {
            return podEventsInfo.clone();
        }
        return new String[0];
    }

    /**
     * setPodEventsInfo.
     *
     * @param podEventsInfo podEventsInfo
     */
    public void setPodEventsInfo(String[] podEventsInfo) {
        if (podEventsInfo != null) {
            this.podEventsInfo = podEventsInfo.clone();
        } else {
            this.podEventsInfo = new String[0];
        }
    }

    /**
     * getContainers.
     *
     * @return
     */
    public PodContainers[] getContainers() {
        if (containers != null) {
            return containers.clone();
        }
        return new PodContainers[0];
    }

    /**
     * setContainers.
     *
     * @param containers containers
     */
    public void setContainers(PodContainers[] containers) {
        if (containers != null) {
            this.containers = containers.clone();
        } else {
            this.containers = new PodContainers[0];
        }
    }

}
