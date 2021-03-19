package org.edgegallery.developer.service.virtual.image;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Date;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_createImageInfo_service")
public class VmImageCreate implements VmImageStage {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmImageCreate.class);

    private static Gson gson = new Gson();

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Override
    public boolean execute(VmImageConfig imageConfig) {
        boolean processSuccess = false;
        boolean instantiateImageResult;

        ApplicationProject project = projectMapper.getProjectById(imageConfig.getProjectId());
        String userId = project.getUserId();
        EnumTestConfigStatus instantiateStatus = EnumTestConfigStatus.Failed;
        VmCreateConfig vmCreateConfig = vmConfigMapper
            .getVmCreateConfig(imageConfig.getProjectId(), imageConfig.getVmId());
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        // deploy app
        try {
            instantiateImageResult = vmService.createVmImageToAppLcm(host, imageConfig, userId);
            if(!instantiateImageResult) {
                LOGGER.error("Failed to  vm image which appInstanceId is : {}.", imageConfig.getAppInstanceId());
            }else {
                // update status when instantiate success
                LOGGER.error("Create vm image success which imageId is : {}.", imageConfig.getImageId());
                imageConfig.setCreateTime(new Date());
                processSuccess = true;
                instantiateStatus = EnumTestConfigStatus.Success;

            }

        } catch (Exception e) {
            imageConfig.setLog("Failed to create vm image with err:" + e.getMessage());
            LOGGER.error("Failed to create vm  image with err: {}.", e.getMessage());
        } finally {
            vmService.updateVmImageResult(imageConfig, project, "createImageInfo", instantiateStatus);
        }
        return processSuccess;
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
