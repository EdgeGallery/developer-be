package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceInfo {

    private String serviceName;

    private String type;

    private ServicePort[] ports;

    /**
     * getPorts.
     *
     * @return
     */
    public ServicePort[] getPorts() {
        if (ports != null) {
            return ports.clone();
        }
        return new ServicePort[0];
    }

    /**
     * setPorts.
     *
     * @param ports ports
     */
    public void setPorts(ServicePort[] ports) {
        if (ports != null) {
            this.ports = ports.clone();
        } else {
            this.ports = null;
        }
    }
}
