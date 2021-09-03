package org.edgegallery.developer.service.application.container;

import org.edgegallery.developer.response.FormatRespDto;
import com.spencerwi.either.Either;

public interface ContainerAppActionService {

    Either<FormatRespDto, Boolean> actionContainer(String applicationId);

    Either<FormatRespDto, Boolean> getContainerDetail(String applicationId);
}
