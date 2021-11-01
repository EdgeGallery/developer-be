/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.model.application.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TrafficFilter {

    private String[] srcAddress;

    private String[] dstAddress;

    private String[] srcPort;

    private String[] dstPort;

    private String[] protocol;

    private String[] tag;

    private String[] srcTunnelAddress;

    private String[] tgtTunnelAddress;

    private String[] srcTunnelPort;

    private String[] dstTunnelPort;

    private Integer qCI;

    private Integer dSCP;

    private Integer tC;

    /**
     * get protocol.
     *
     * @return
     */
    public String[] getProtocol() {
        if (this.protocol != null) {
            return this.protocol.clone();
        } else {
            return new String[0];
        }
    }

    /**
     * set protocol.
     *
     * @param protocol protocol
     */
    public void setProtocol(String[] protocol) {
        if (protocol != null) {
            this.protocol = protocol.clone();
        } else {
            this.protocol = null;
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
