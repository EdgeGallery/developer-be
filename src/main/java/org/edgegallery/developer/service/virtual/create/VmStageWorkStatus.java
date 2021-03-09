package org.edgegallery.developer.service.virtual.create;

import java.lang.reflect.Type;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(config.getHost()), type);
//        String workStatus = HttpClientUtil
//            .getWorkloadStatus(host.getProtocol(), host.getIp(), host.getPort(), config.getAppInstanceId(), userId,
//                config.getLcmToken());
        String workStatus = "{\"pods\":[{\"podstatus\":\"Running\",\"podname\":\"positioning\",\"containers\":[{\"containername\":\"positioning\",\"metricsusage\":{\"cpuusage\":\"90/4000\",\"memusage\":\"81469440/16714080256\",\"diskusage\":\"0/94877588119\"}},{\"containername\":\"mep-agent\",\"metricsusage\":{\"cpuusage\":\"0/4000\",\"memusage\":\"8871936/16714080256\",\"diskusage\":\"0/94877588119\"}}]}]}";
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
            JsonObject jsonObject = new JsonParser().parse(workStatus).getAsJsonObject();
            // set vmInfo todo
//            config.setVmInfo();
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