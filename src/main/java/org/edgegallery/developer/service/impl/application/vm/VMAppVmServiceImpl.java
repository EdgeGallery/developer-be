package org.edgegallery.developer.service.impl.application.vm;

import java.util.List;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;
@Service("vmAppVmService")
public class VMAppVmServiceImpl implements VMAppVmService {

    @Override
    public Either<FormatRespDto, VirtualMachine> createVm(String applicationId, VirtualMachine virtualMachine) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<VirtualMachine>> getAllVm(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, VirtualMachine> getVm(String applicationId, String vmId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteVm(String applicationId, String vmId) {
        return null;
    }
}
