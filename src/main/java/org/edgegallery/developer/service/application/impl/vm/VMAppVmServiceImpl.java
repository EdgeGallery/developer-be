package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.VMMapper;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
@Service("vmAppVmService")
public class VMAppVmServiceImpl implements VMAppVmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static Gson gson = new Gson();

    @Autowired
    VMMapper vmMapper;

    @Override
    public Either<FormatRespDto, VirtualMachine> createVm(String applicationId, VirtualMachine virtualMachine) {
        int res = vmMapper.createVM(applicationId, virtualMachine);
        if (res < 1) {
            LOGGER.error("Create vm in db error.");
            throw new DeveloperException("Create vm in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        return Either.right(virtualMachine);
    }

    @Override
    public List<VirtualMachine> getAllVm(String applicationId) {
        return vmMapper.getAllVMsByAppId(applicationId);
    }

    @Override
    public VirtualMachine getVm(String applicationId, String vmId) {
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
        int res = vmMapper.deleteVM(vmId);
        if (res < 1) {
            LOGGER.error("delete vm in db error.");
            throw new DeveloperException("delete vm in db error.", ResponseConsts.DELETE_DATA_FAILED);
        }
        return Either.right(true);
    }
}
