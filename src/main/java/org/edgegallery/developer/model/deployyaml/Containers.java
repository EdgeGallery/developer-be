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
        if (env.length >= 1) {
            StringBuilder sb = new StringBuilder();
            for (Environment en : env) {
                sb.append(en.getName());
                sb.append(en.getValue());
            }
            if (StringUtils.isNotEmpty(sb.toString())) {
                this.env = env;
            }
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
