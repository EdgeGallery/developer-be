package org.edgegallery.developer.service.virtual.image;

import org.edgegallery.developer.model.vm.VmImageConfig;

public interface VmImageStage {
    boolean execute(VmImageConfig config) throws InterruptedException;

    boolean destroy();

    boolean immediateExecute(VmImageConfig config);

}
