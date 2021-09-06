package org.edgegallery.developer.service.application.impl.vm;

import java.util.List;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.application.vm.NetworkMapper;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;
@Service("vmAppNetworkService")
public class VMAppNetworkServiceImpl implements VMAppNetworkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    NetworkMapper networkMapper;

    @Override
    public Either<FormatRespDto, Network> createNetwork(String applicationId, Network network) {
        int res = networkMapper.createNetwork(applicationId, network);
        if (res < 1) {
            LOGGER.error("Create network in db error.");
            throw new DeveloperException("Create network in db error.", ResponseConsts.INSERT_DATA_FAILED);
        }
        return Either.right(network);
    }

    @Override
    public List<Network> getAllNetwork(String applicationId) {
        return networkMapper.getNetworkByAppId(applicationId);
    }

    @Override
    public Network getNetwork(String applicationId, String networkId) {
        return networkMapper.getNetworkById(networkId);

    }

    @Override
    public Either<FormatRespDto, Boolean> modifyNetwork(String applicationId, String networkId, Network network) {
        int res = networkMapper.modifyNetwork(network);
        if (res < 1) {
            LOGGER.error("modify network in db error.");
            throw new DeveloperException("modify network in db error.", ResponseConsts.MODIFY_DATA_FAILED);
        }
        return Either.right(true);
    }

    @Override
    public Either<FormatRespDto, Boolean> deleteNetwork(String applicationId, String networkId) {
        int res = networkMapper.deleteNetwork(networkId);
        if (res < 1) {
            LOGGER.error("delete network in db error.");
            throw new DeveloperException("delete network in db error.", ResponseConsts.DELETE_DATA_FAILED);
        }
        return Either.right(true);
    }
}
