package org.edgegallery.developer.service.application.vm;

import javax.servlet.http.HttpServletRequest;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.response.FormatRespDto;
import org.springframework.http.ResponseEntity;
import com.spencerwi.either.Either;

public interface VmAppActionService {


    Either<FormatRespDto, Boolean> actionVm(String applicationId, String vmId);

    Either<FormatRespDto, Boolean> uploadFileToVm(String applicationId, String vmId, HttpServletRequest request, Chunk chunk);

    ResponseEntity mergeAppFile(String applicationId, String vmId, String fileName, String identifier);
}
