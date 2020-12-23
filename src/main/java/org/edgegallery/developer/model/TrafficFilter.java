package org.edgegallery.developer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenhui
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficFilter {

    private String[] protocal;

    private String[] srcAddress;

    private Integer qCI;

    private String[] srcPort;

    private Integer dSCP;

    private String[] dstAddress;

    private Integer tC;

    private String[] dstPort;

    private String[] tag;

    private String[] srcTunnelAddress;

    private String[] tgtTunnelAddress;

    private String[] srcTunnelPort;

    private String[] dstTunnelPort;

    /**
     * getProtocal.
     * @return
     */
    public String[] getProtocal() {
        if (this.protocal != null) {
            return protocal.clone();
        } else {
            return null;
        }
    }

    /**
     * setProtocal.
     * @param protocal
     */
    public void setProtocal(String[] protocal) {
        if (protocal != null) {
            this.protocal = protocal.clone();
        } else {
            this.protocal = null;
        }
    }
}
