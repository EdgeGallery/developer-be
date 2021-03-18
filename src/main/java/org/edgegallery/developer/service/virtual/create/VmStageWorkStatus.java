package org.edgegallery.developer.service.virtual.create;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmInstantiateInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_workStatus_service")
public class VmStageWorkStatus implements VmCreateStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmStageWorkStatus.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 360L;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(config.getHost()), type);

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String workStatus =HttpClientUtil.getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(),
            config.getAppInstanceId(), project.getUserId(), config.getLcmToken());
        if (workStatus == null) {
            // compare time between now and deployDate
            long time = System.currentTimeMillis() - config.getCreateTime().getTime();
            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
                config.getCreateTime().getTime());
            if (config.getCreateTime() == null || time > MAX_SECONDS * 1000) {
                config.setLog("Failed to get create vm result ");
                String message = "Failed to get create vm result after wait {} seconds which appInstanceId is : {}";
                LOGGER.error(message, MAX_SECONDS, config.getAppInstanceId());
            } else {
                return true;
            }
        } else {
            processStatus = true;
            status = EnumTestConfigStatus.Success;
            config.setLog("get vm status success");
            Type vmInfoType = new TypeToken<VmInstantiateInfo>() { }.getType();
            VmInstantiateInfo vmInstantiateInfo = gson.fromJson(workStatus, vmInfoType);
            config.setVmInfo(vmInstantiateInfo.getData());

            LOGGER.info("Query create vm info response: {}", workStatus);
        }
        // update test-config
        vmService.updateCreateVmResult(config, project, "workStatus", status);
        return processStatus;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(VmCreateConfig config) {
        return true;
    }
}
