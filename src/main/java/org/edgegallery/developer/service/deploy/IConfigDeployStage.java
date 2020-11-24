package org.edgegallery.developer.service.deploy;

import org.edgegallery.developer.model.workspace.ProjectTestConfig;

/**
 * @author chenhui
 */
public interface IConfigDeployStage {

    boolean execute(ProjectTestConfig config) throws InterruptedException;

    boolean destroy();

    boolean immediateExecute(ProjectTestConfig config);
}
