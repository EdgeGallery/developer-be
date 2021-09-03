package org.edgegallery.developer.service.application.vm;

import java.util.List;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.response.FormatRespDto;
import com.spencerwi.either.Either;

public interface VMAppNetworkService {

    Either<FormatRespDto, Network> createNetwork(String applicationId, Network network);

    Either<FormatRespDto, List<Network>> getAllNetwork(String applicationId);

    Either<FormatRespDto, Network> getNetwork(String applicationId, String networkId);

    Either<FormatRespDto, Boolean> modifyNetwork(String applicationId, String networkId, Network network);

    Either<FormatRespDto, Boolean> deleteNetwork(String applicationId, String networkId);
}
