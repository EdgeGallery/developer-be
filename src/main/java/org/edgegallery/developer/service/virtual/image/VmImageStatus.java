package org.edgegallery.developer.service.virtual.image;

import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.service.virtual.VmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_imageStatus_service")
public class VmImageStatus implements VmImageStage {

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(VmImageConfig config) throws InterruptedException {
        config.setChunkSize(10);
        config.setSumChunkNum(10);
        config.setHostIp("119.8.47.5");
        config.setImageName("image_test");
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        boolean processStatus = false;
        processStatus = true;
        status = EnumTestConfigStatus.Success;
        // update test-config
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        vmService.updateVmImageResult(config, project, "imageStatus", status);
        return processStatus;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(VmImageConfig config) {
        return true;
    }

}
