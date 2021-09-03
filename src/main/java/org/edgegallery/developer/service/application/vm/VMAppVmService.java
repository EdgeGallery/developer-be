package org.edgegallery.developer.service.application.vm;

import java.util.List;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.FormatRespDto;
import com.spencerwi.either.Either;

public interface VMAppVmService {

    Either<FormatRespDto, VirtualMachine> createVm(String applicationId, VirtualMachine virtualMachine);

    Either<FormatRespDto, List<VirtualMachine>> getAllVm(String applicationId);

    Either<FormatRespDto, VirtualMachine> getVm(String applicationId, String vmId);

    Either<FormatRespDto, Boolean> modifyVm(String applicationId, String vmId, VirtualMachine virtualMachine);

    Either<FormatRespDto, Boolean> deleteVm(String applicationId, String vmId);
}
