package org.edgegallery.developer.model.lcm;

public class DistributeBody {
    private String[] hostIp;

    /**
     * getHostIp.
     *
     * @return
     */
    public String[] getHostIp() {
        if (hostIp != null) {
            return hostIp.clone();
        }
        return new String[0];
    }

    /**
     * setHostIp.
     *
     * @param hostIp hostIp
     */
    public void setHostIp(String[] hostIp) {
        if (hostIp != null) {
            this.hostIp = hostIp.clone();
        } else {
            this.hostIp = null;
        }
    }
}
