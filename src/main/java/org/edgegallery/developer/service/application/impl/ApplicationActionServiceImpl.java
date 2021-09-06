package org.edgegallery.developer.service.application.impl;

import org.edgegallery.developer.model.application.SelectSandbox;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.ApplicationActionService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("applicationActionService")
public class ApplicationActionServiceImpl implements ApplicationActionService {

    @Override
    public Either<FormatRespDto, Boolean> cleanEnv(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> generatePackage(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> commitTest(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> selectSandbox(String applicationId, SelectSandbox selectSandbox) {
        return null;
    }
}
