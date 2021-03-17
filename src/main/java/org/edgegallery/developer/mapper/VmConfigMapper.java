package org.edgegallery.developer.mapper;

import java.util.List;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmFlavor;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmSystem;

public interface VmConfigMapper {

    List<VmRegulation> getVmRegulation();

    List<VmSystem> getVmSystem();

    List<VmNetwork> getVmNetwork();

    int saveVmCreateConfig(VmCreateConfig vmCreateConfig);

    int updateVmCreateConfig(VmCreateConfig testConfig);

    VmCreateConfig getVmCreateConfig(String projectId, String vmId);

    List<VmCreateConfig> getVmCreateConfigs(String projectId);

    List<VmCreateConfig> getVmCreateConfigStatus(String toString);

    int deleteVmCreateConfig(String projectId, String vmId);

    int deleteVmCreateConfigs(String projectId);

    VmFlavor getVmFlavor(String architecture);

    int saveVmImageConfig(VmImageConfig vmImageConfig);

    VmImageConfig getVmImage(String projectId, String vmId);

    int deleteVmImage(String projectId, String vmId);

    int updateVmImageConfig(VmImageConfig config);

    List<VmImageConfig> getVmImageConfigStatus(String toString);
}
