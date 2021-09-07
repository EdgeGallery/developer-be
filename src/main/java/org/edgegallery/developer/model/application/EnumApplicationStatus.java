package org.edgegallery.developer.model.application;

public enum EnumApplicationStatus {
    ONLINE("Online"),
    DEPLOYING("Deploying"),
    DEPLOYED("Deployed"),
    DEPLOYED_FAILED("DeployFailed"),
    TESTING("Testing"),
    TESTED("Tested"),
    RELEASED("Released");

    private String name;

    EnumApplicationStatus(String name) {
        this.name = name;
    }
}
