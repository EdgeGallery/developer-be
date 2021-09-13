package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.model.application.vm.VMPort;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
@Service("vmAppVmService")
public class VMAppVmServiceImpl implements VMAppVmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static Gson gson = new Gson();

    @Autowired
    VMMapper vmMapper;

    @Autowired
    VMAppOperationServiceImpl vmAppOperationServiceImpl;

    @Override
    public Either<FormatRespDto, VirtualMachine> createVm(String applicationId, VirtualMachine virtualMachine) {
        virtualMachine.setId(UUID.randomUUID().toString());
        int res = vmMapper.createVM(applicationId, virtualMachine);
        if (res < 1) {
            LOGGER.error("Create vm in db error.");
            throw new DeveloperException("Create vm in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        if (virtualMachine.getVmCertificate() != null) {
            vmMapper.createVMCertificate(virtualMachine.getId(),virtualMachine.getVmCertificate());
        }
        if (!CollectionUtils.isEmpty(virtualMachine.getPortList())) {
            for (VMPort port:virtualMachine.getPortList()) {
                vmMapper.createVMPort(virtualMachine.getId(), port);
            }
        }
        return Either.right(virtualMachine);
    }

    @Override
    public List<VirtualMachine> getAllVm(String applicationId) {
        List<VirtualMachine> virtualMachines = vmMapper.getAllVMsByAppId(applicationId);

        for (VirtualMachine virtualMachine:virtualMachines) {
            virtualMachine.setVmInstantiateInfo(vmAppOperationServiceImpl.getInstantiateInfo(virtualMachine.getId()));
            virtualMachine.setImageExportInfo(vmAppOperationServiceImpl.getImageExportInfo(virtualMachine.getId()));
            virtualMachine.setPortList(vmMapper.getAllVMPortsByVMId(virtualMachine.getId()));
            virtualMachine.setVmCertificate(vmMapper.getVMCertificate(virtualMachine.getId()));
        }
        return virtualMachines;
    }

    @Override
    public VirtualMachine getVm(String applicationId, String vmId) {
        VirtualMachine virtualMachine = vmMapper.getVMById(vmId);
        virtualMachine.setImageExportInfo(vmAppOperationServiceImpl.getImageExportInfo(virtualMachine.getId()));
        virtualMachine.setVmInstantiateInfo(vmAppOperationServiceImpl.getInstantiateInfo(virtualMachine.getId()));
        return vmMapper.getVMById(vmId);
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine) {
        int res = vmMapper.modifyVM(virtualMachine);
        if (res < 1) {
            LOGGER.error("modify vm in db error.");
            throw new DeveloperException("modify vm in db error.", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return Either.right(true);
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteVm(String applicationId, String vmId) {
        // todo delete package instantiate and image
        int res = vmMapper.deleteVM(vmId);
        if (res < 1) {
            LOGGER.error("delete vm in db error.");
            throw new DeveloperException("delete vm in db error.", ResponseConsts.DELETE_DATA_FAILED);
        }
        return Either.right(true);
    }
}
