package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodImage {
    private String podName;

    private String[] podImage;

    /**
     * getPodImage.
     *
     * @return
     */
    public String[] getPodImage() {
        if (podImage != null) {
            return podImage.clone();
        }
        return new String[0];
    }

    /**
     * setPodImage.
     *
     * @param podImage podImage
     */
    public void setPodImage(String[] podImage) {
        if (podImage != null) {
            this.podImage = podImage.clone();
        } else {
            this.podImage = null;
        }
    }

}