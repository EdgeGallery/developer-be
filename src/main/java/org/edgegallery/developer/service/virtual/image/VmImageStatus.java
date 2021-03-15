package org.edgegallery.developer.service.virtual.image;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@Service("vm_imageStatus_service")
public class VmImageStatus implements VmImageStage{
    private static final Logger LOGGER = LoggerFactory.getLogger(VmImageStatus.class);

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
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(config.getProjectId(), config.getVmId());
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
//        String imageInfo = HttpClientUtil
//            .getImageStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(), userId, config.getImageId(),
//                config.getLcmToken());
//        JsonObject jsonObject = new JsonParser().parse(imageInfo).getAsJsonObject();
//        JsonElement imageStatus = jsonObject.get("status");
//        if (!imageStatus.getAsString().equals("success")) {
//            // compare time between now and deployDate
//            long time = System.currentTimeMillis() - config.getCreateTime().getTime();
//            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
//                config.getCreateTime().getTime());
//            if (config.getCreateTime() == null || time > MAX_SECONDS * 1000) {
//                config.setLog("vm image is " + imageStatus.getAsString());
//            } else {
//                return true;
//            }
//        } else {
//            processStatus = true;
//            status = EnumTestConfigStatus.Success;
//            // set vmImageConfig todo
////            config.setVmInfo();
//            LOGGER.info("Query vm image info response: {}", imageInfo);
//        }
        // test date
        config.setChunkSize(10);
        config.setSumChunkNum(10);
        config.setHostIp("119.8.47.5");
        config.setImageName("image_test");
        processStatus = true;
        status = EnumTestConfigStatus.Success;
        // update test-config

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
