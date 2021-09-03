package org.edgegallery.developer.service.application;

import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.response.FormatRespDto;
import org.springframework.transaction.annotation.Transactional;
import com.spencerwi.either.Either;

public interface ApplicationService {

    /**
     * create a application
     *
     * @param application application
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Application> createApplication(String userId, Application application);

    /**
     * get a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Application> getApplication(String applicationId);

    /**
     * modify a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Either<FormatRespDto, Boolean> modifyApplication(String applicationId, Application application);

    /**
     * get a application
     *
     * @param userId userId
     * @return
     */
    @Transactional
    Page<Application> getApplicationByNameWithFuzzy(String userId, String projectName, int limit, int offset);

    /**
     * DELETE a application
     *
     * @param applicationId applicationId
     * @return
     */
    @Transactional
    Either<FormatRespDto, Boolean> deleteApplication(String applicationId);

}
