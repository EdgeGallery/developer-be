package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodEvents {

    private String podName;

    private String[] podEventsInfo;

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
}
