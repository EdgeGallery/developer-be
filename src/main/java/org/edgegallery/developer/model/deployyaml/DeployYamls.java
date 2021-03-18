package org.edgegallery.developer.model.deployyaml;

public class DeployYamls {
    private DeployYaml[] deployYamls;

    /**
     * getDeployYamls.
     *
     * @return
     */
    public DeployYaml[] getDeployYamls() {
        if (deployYamls != null) {
            return deployYamls.clone();
        }
        return new DeployYaml[0];
    }

    /**
     * setDeployYamls.
     *
     * @param deployYamls deployYamls
     */
    public void setDeployYamls(DeployYaml[] deployYamls) {
        if (deployYamls != null) {
            this.deployYamls = deployYamls.clone();
        } else {
            this.deployYamls = null;
        }
    }
}
