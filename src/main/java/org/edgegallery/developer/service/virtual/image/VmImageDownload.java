package org.edgegallery.developer.service.virtual.image;

import static org.edgegallery.developer.util.AtpUtil.getProjectPath;

import java.io.File;
import java.lang.reflect.Type;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service("vm_downloadImageInfo_service")
public class VmImageDownload implements VmImageStage{
    private static final Logger LOGGER = LoggerFactory.getLogger(VmImageDownload.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 360L;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;


    @Override
    public boolean execute(VmImageConfig config) throws InterruptedException {
        boolean processStatus = false;
        boolean downloadImageResult;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        String packagePath = getProjectPath(config.getProjectId()) + config.getAppInstanceId() ;
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(config.getProjectId(), config.getVmId());
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        // download image
        try {
            downloadImageResult = HttpClientUtil
                .downloadVmImage(host.getProtocol(), host.getIp(), host.getPort(), userId, packagePath, config);
            downloadImageResult = true;
            if(!downloadImageResult) {
                LOGGER.error("Failed to download image which appInstanceId is : {}.", config.getAppInstanceId());
            }else {
                processStatus = true;
                status = EnumTestConfigStatus.Success;
            }

        }catch (Exception e) {
            config.setLog("Failed to download image with err:" + e.getMessage());
            LOGGER.error("Failed to download image with err: {}.", e.getMessage());
        } finally {
            vmService.updateVmImageResult(config, project, "downloadImageInfo", status);
        }

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