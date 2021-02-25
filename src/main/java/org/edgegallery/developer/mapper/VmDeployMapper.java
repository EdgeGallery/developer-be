package org.edgegallery.developer.mapper;

import java.util.List;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.vm.VmSystem;

public interface VmDeployMapper {

    VmResource getVmResource();

    List<VmRegulation> getVmRegulation();

    List<VmSystem> getVmSystem();

    List<VmNetwork> getVmNetwork();
}
