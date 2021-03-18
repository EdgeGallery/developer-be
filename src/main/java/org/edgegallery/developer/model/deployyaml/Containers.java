package org.edgegallery.developer.model.deployyaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class Containers {
    private String name;

    private String image;

    private String imagePullPolicy;

    private Environment[] env;

    private Ports[] ports;

    private VolumeMounts[] volumeMounts;

    private String command;

    private Resource resources;

    /**
     * get env.
     *
     * @return
     */
    public Environment[] getEnv() {
        if (env != null) {
            return env.clone();
        }
        return new Environment[0];
    }

    /**
     * getPorts.
     *
     * @return
     */
    public Ports[] getPorts() {
        if (ports != null) {
            return ports.clone();
        }
        return new Ports[0];
    }

    /**
     * getVolumeMounts.
     *
     * @return
     */
    public VolumeMounts[] getVolumeMounts() {
        if (volumeMounts != null) {
            return volumeMounts.clone();
        }
        return new VolumeMounts[0];
    }

    /**
     * setPorts.
     *
     * @param ports ports
     */
    public void setPorts(Ports[] ports) {
        if (ports != null) {
            this.ports = ports.clone();
        } else {
            this.ports = null;
        }

    }

    /**
     * setVolumeMounts.
     *
     * @param volumeMounts vols
     */
    public void setVolumeMounts(VolumeMounts[] volumeMounts) {
        if (volumeMounts != null) {
            this.volumeMounts = volumeMounts.clone();
        } else {
            this.volumeMounts = null;
        }
    }

    /**
     * set command.
     *
     * @param command command
     */
    public void setCommand(String command) {
        if (StringUtils.isNotEmpty(command)) {
            this.command = command;
        }
    }

    /**
     * set env.
     *
     * @param env env
     */
    public void setEnv(Environment[] env) {
        if (env != null) {
            if (env.length >= 1) {
                StringBuilder sb = new StringBuilder();
                for (Environment en : env) {
                    sb.append(en.getName());
                    sb.append(en.getValue());
                }
                if (StringUtils.isNotEmpty(sb.toString())) {
                    this.env = env.clone();
                }
            }
        } else {
            this.env = null;
        }
    }

    /**
     * set resources.
     *
     * @param resources resources
     */
    public void setResources(Resource resources) {
        String limitCpu = resources.getLimits().getCpu();
        String limitMeo = resources.getLimits().getMemory();
        String reqCpu = resources.getRequests().getCpu();
        String reqMeo = resources.getRequests().getMemory();
        if (StringUtils.isNotEmpty(limitCpu) && StringUtils.isNotEmpty(limitMeo) && StringUtils.isNotEmpty(reqCpu)
            && StringUtils.isNotEmpty(reqMeo)) {
            this.resources = resources;
        }
    }
}
