package org.edgegallery.developer.service.application.impl.vm;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMInstantiateInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchOperation;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.impl.ApplicationServiceImpl;
import org.edgegallery.developer.service.application.vm.VmAppOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("vmAppOperationService")
public class VMAppOperationServiceImpl extends AppOperationServiceImpl implements VmAppOperationService {

    public static final String OPERATION_NAME = "VirtualMachine launch";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    VMInstantiateInfoMapper vmInstantiateInfoMapper;

    @Autowired
    ImageExportInfoMapper imageExportInfoMapper;

    @Autowired
    OperationStatusMapper operationStatusMapper;

    @Autowired
    ApplicationServiceImpl applicationServiceImpl;

    @Autowired
    VMAppVmServiceImpl vmAppVmServiceImpl;

    @Autowired
    VMMapper vmMapper;

    @Override
    public OperationInfoRep instantiateVmApp(String applicationId, String vmId, String accessToken) {

        Application application = applicationServiceImpl.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application is not exited,id:{}", applicationId);
            throw new DeveloperException("application is not exited.", ResponseConsts.APPLICATION_NOT_EXIT);
        }

        VirtualMachine virtualMachine = vmAppVmServiceImpl.getVm(applicationId, vmId);
        if (virtualMachine==null || virtualMachine.getVmInstantiateInfo()!=null
            || virtualMachine.getImageExportInfo() != null) {
            LOGGER.error("instantiate vm app fail ,vm is not exit or is used,vmId:{}", vmId);
            throw new DeveloperException("instantiate vm app fail ,vm is not exit or is used.", ResponseConsts.INSTANTIATE_VM_FAIL);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setObjectType(EnumOperationObjectType.APPLICATION_INSTANCE);
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setObjectId(vmId);
        operationStatus.setOperationName(OPERATION_NAME);
        int res = operationStatusMapper.createOperationStatus(operationStatus);
        if (res < 1) {
            LOGGER.error("Create operationStatus in db error.");
            throw new DeveloperException("Create operationStatus in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        VMLaunchOperation actionCollection = new VMLaunchOperation(accessToken, operationStatus);
        LOGGER.info("start instantiate vm app");
        new InstantiateVmAppProcessor(actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public Boolean uploadFileToVm(String applicationId, String vmId, HttpServletRequest request,
        Chunk chunk) {
        return null;
    }

    @Override
    public ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier) {
        return null;
    }


    @Override
    public Boolean generatePackage(String applicationId) {
        return null;
    }

    public VMInstantiateInfo getInstantiateInfo(String vmId) {
        return vmInstantiateInfoMapper.getVMInstantiateInfo(vmId);
    }

    public ImageExportInfo getImageExportInfo(String vmId) {
        return imageExportInfoMapper.getImageExportInfoInfoByVMId(vmId);
    }

    public static class InstantiateVmAppProcessor extends Thread {

        VMLaunchOperation actionCollection;

        public InstantiateVmAppProcessor(VMLaunchOperation actionCollection) {
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            IActionIterator iterator = actionCollection.getActionIterator();
            while (iterator.hasNext()) {
                IAction action = iterator.nextAction();
                boolean result = action.execute();
                if (!result) {
                    break;
                }
            }
        }
    }

}
