package org.edgegallery.developer.service.application;

import org.edgegallery.developer.model.application.SelectSandbox;
import org.edgegallery.developer.response.FormatRespDto;
import com.spencerwi.either.Either;

public interface ApplicationActionService {

    Either<FormatRespDto, Boolean> cleanEnv(String applicationId);

    Either<FormatRespDto, Boolean> generatePackage(String applicationId);

    Either<FormatRespDto, Boolean> commitTest(String applicationId);

    Either<FormatRespDto, Boolean> selectSandbox(String applicationId, SelectSandbox selectSandbox);
}
