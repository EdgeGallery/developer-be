package org.edgegallery.developer.service.impl.application.vm;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.vm.VmAppActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("vmAppActionService")
public class VMAppActionServiceImpl implements VmAppActionService {

    @Override
    public Either<FormatRespDto, Boolean> actionVm(String applicationId, String vmId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> uploadFileToVm(String applicationId, String vmId, HttpServletRequest request,
        Chunk chunk) {
        return null;
    }

    @Override
    public ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier) {
        return null;
    }
}
