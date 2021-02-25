package org.edgegallery.developer.service.virtual;

import java.util.List;
import org.edgegallery.developer.mapper.VmDeployMapper;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spencerwi.either.Either;

@Service("vmService")
public class VmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    @Autowired
    private VmDeployMapper vmDeployMapper;

    public Either<FormatRespDto, VmResource> getVirtualResource() {
        List<VmRegulation> vmRegulation = vmDeployMapper.getVmRegulation();
        List<VmSystem> vmSystem = vmDeployMapper.getVmSystem();
        List<VmNetwork> vmNetwork = vmDeployMapper.getVmNetwork();
        VmResource vmResource = new VmResource();
        vmResource.setVmRegulationList(vmRegulation);
        vmResource.setVmSystemList(vmSystem);
        vmResource.setVmNetworkList(vmNetwork);
        LOGGER.info("Get all vm resource success");
        return Either.right(vmResource);

    }

}

