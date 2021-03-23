package org.edgegallery.developer.service.virtual.image;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmImageInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_imageStatus_service")
public class VmImageStatus implements VmImageStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmImageStatus.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 120L;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Override
    public boolean execute(VmImageConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(config.getProjectId(), config.getVmId());
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        String workStatus = HttpClientUtil
            .getImageStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(),
                project.getUserId(), config.getImageId(), config.getLcmToken());
        LOGGER.info("import image result: {}", workStatus);
        if (workStatus == null) {
            // compare time between now and deployDate
            long time = System.currentTimeMillis() - config.getCreateTime().getTime();
            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
                config.getCreateTime().getTime());
            if (config.getCreateTime() == null || time > MAX_SECONDS * 1000) {
                config.setLog("Failed to get vm image result ");
                String message = "Failed to get vm image result after wait {} seconds which appInstanceId is : {}";
                LOGGER.error(message, MAX_SECONDS, config.getAppInstanceId());
            } else {
                return true;
            }
        } else {
            Type vmInfoType = new TypeToken<VmImageInfo>() { }.getType();
            VmImageInfo vmImageInfo = gson.fromJson(workStatus, vmInfoType);
            if (vmImageInfo.getStatus().equals("active")) {
                processStatus = true;
                status = EnumTestConfigStatus.Success;
                config.setLog("get vm status success");
                config.setImageName(vmImageInfo.getImageName());
                config.setSumChunkNum(vmImageInfo.getSumChunkNum());
                config.setChunkSize(vmImageInfo.getChunkSize());
                // get config
                LOGGER.info("update config result:{}", config);
            } else {
                return true;
            }

        }
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
