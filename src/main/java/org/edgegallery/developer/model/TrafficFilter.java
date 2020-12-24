package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficFilter {

    private String[] protocal;

    private String[] srcAddress;

    @Getter
    @Setter
    private Integer qCI;

    private String[] srcPort;

    @Getter
    @Setter
    private Integer dSCP;

    private String[] dstAddress;

    @Getter
    @Setter
    private Integer tC;

    private String[] dstPort;

    private String[] tag;

    private String[] srcTunnelAddress;

    private String[] tgtTunnelAddress;

    private String[] srcTunnelPort;

    private String[] dstTunnelPort;

    /**
     * getProtocal.
     *
     * @return
     */
    public String[] getProtocal() {
        if (this.protocal != null) {
            return this.protocal.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setProtocal.
     *
     * @param protocal protocal
     */
    public void setProtocal(String[] protocal) {
        if (protocal != null) {
            this.protocal = protocal.clone();
        } else {
            this.protocal = null;
        }
    }

    /**
     * getSrcAddress.
     *
     * @return
     */
    public String[] getSrcAddress() {
        if (this.srcAddress != null) {
            return this.srcAddress.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * srcAddress.
     *
     * @param srcAddress srcAddress
     */
    public void setSrcAddress(String[] srcAddress) {
        if (srcAddress != null) {
            this.srcAddress = srcAddress.clone();
        } else {
            this.srcAddress = null;
        }
    }

    /**
     * getSrcPort.
     *
     * @return
     */
    public String[] getSrcPort() {
        if (this.srcPort != null) {
            return this.srcPort.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setSrcPort.
     *
     * @param srcPort srcPort
     */
    public void setSrcPort(String[] srcPort) {
        if (srcPort != null) {
            this.srcPort = srcPort.clone();
        } else {
            this.srcPort = null;
        }
    }

    /**
     * getDstAddress.
     *
     * @return
     */
    public String[] getDstAddress() {
        if (this.dstAddress != null) {
            return this.dstAddress.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setDstAddress.
     *
     * @param dstAddress dstAddress
     */
    public void setDstAddress(String[] dstAddress) {
        if (dstAddress != null) {
            this.dstAddress = dstAddress.clone();
        } else {
            this.dstAddress = null;
        }
    }

    /**
     * getDstPort.
     *
     * @return
     */
    public String[] getDstPort() {
        if (this.dstPort != null) {
            return this.dstPort.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setDstPort.
     *
     * @param dstPort dstPort
     */
    public void setDstPort(String[] dstPort) {
        if (dstPort != null) {
            this.dstPort = dstPort.clone();
        } else {
            this.dstPort = null;
        }
    }

    /**
     * getTag.
     *
     * @return
     */
    public String[] getTag() {
        if (this.tag != null) {
            return this.tag.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setTag.
     *
     * @param tag tag
     */
    public void setTag(String[] tag) {
        if (tag != null) {
            this.tag = tag.clone();
        } else {
            this.tag = null;
        }
    }

    /**
     * getSrcTunnelAddress.
     *
     * @return
     */
    public String[] getSrcTunnelAddress() {
        if (this.srcTunnelAddress != null) {
            return this.srcTunnelAddress.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setSrcTunnelAddress.
     *
     * @param srcTunnelAddress srcTunnelAddress
     */
    public void setSrcTunnelAddress(String[] srcTunnelAddress) {
        if (srcTunnelAddress != null) {
            this.srcTunnelAddress = srcTunnelAddress.clone();
        } else {
            this.srcTunnelAddress = null;
        }
    }

    /**
     * getTgtTunnelAddress.
     *
     * @return
     */
    public String[] getTgtTunnelAddress() {
        if (this.tgtTunnelAddress != null) {
            return this.tgtTunnelAddress.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setTgtTunnelAddress.
     *
     * @param tgtTunnelAddress tgtTunnelAddress
     */
    public void setTgtTunnelAddress(String[] tgtTunnelAddress) {
        if (tgtTunnelAddress != null) {
            this.tgtTunnelAddress = tgtTunnelAddress.clone();
        } else {
            this.tgtTunnelAddress = null;
        }
    }

    /**
     * getSrcTunnelPort.
     *
     * @return
     */
    public String[] getSrcTunnelPort() {
        if (this.srcTunnelPort != null) {
            return this.srcTunnelPort.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setSrcTunnelPort.
     *
     * @param srcTunnelPort srcTunnelPort
     */
    public void setSrcTunnelPort(String[] srcTunnelPort) {
        if (srcTunnelPort != null) {
            this.srcTunnelPort = srcTunnelPort.clone();
        } else {
            this.srcTunnelPort = null;
        }
    }

    /**
     * getDstTunnelPort.
     *
     * @return
     */
    public String[] getDstTunnelPort() {
        if (this.dstTunnelPort != null) {
            return this.dstTunnelPort.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * setDstTunnelPort.
     *
     * @param dstTunnelPort dstTunnelPort
     */
    public void setDstTunnelPort(String[] dstTunnelPort) {
        if (dstTunnelPort != null) {
            this.dstTunnelPort = dstTunnelPort.clone();
        } else {
            this.dstTunnelPort = null;
        }
    }
}
