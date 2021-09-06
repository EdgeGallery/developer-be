package org.edgegallery.developer.service.application.impl;

import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.ApplicationService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {

    @Override
    public Either<FormatRespDto, Application> createApplication(Application application) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Application> getApplication(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyApplication(String applicationId, Application application) {
        return null;
    }

    @Override
    public Page<Application> getApplicationByNameWithFuzzy(String userId, String projectName, int limit, int offset) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteApplication(String applicationId) {
        return null;
    }
}
