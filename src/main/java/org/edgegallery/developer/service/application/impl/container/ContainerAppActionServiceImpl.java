package org.edgegallery.developer.service.application.impl.container;

import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.container.ContainerAppActionService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;
@Service("containerAppActionService")
public class ContainerAppActionServiceImpl implements ContainerAppActionService {

    @Override
    public Either<FormatRespDto, Boolean> actionContainer(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> getContainerDetail(String applicationId) {
        return null;
    }
}
