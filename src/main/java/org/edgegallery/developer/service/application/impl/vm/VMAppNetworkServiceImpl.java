package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;
@Service("vmAppNetworkService")
public class VMAppNetworkServiceImpl implements VMAppNetworkService {

    @Override
    public Either<FormatRespDto, Network> createNetwork(String applicationId, Network network) {
        return null;
    }

    @Override
    public Either<FormatRespDto, List<Network>> getAllNetwork(String applicationId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Network> getNetwork(String applicationId, String networkId) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> modifyNetwork(String applicationId, String networkId, Network network) {
        return null;
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteNetwork(String applicationId, String networkId) {
        return null;
    }
}
