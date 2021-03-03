package org.edgegallery.developer.service.csar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class NewCreateVmCsar {
    private static final String simpleFiles = "/app-name.mf";

    private static final String WORKSPACE_CSAR_PATH = "./configs/vm_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/APPD/Definition/Eastcom-SPCLNLWY-EMS_eulerforTR6_iso.yaml";

    private static final String IMAGE_BASE_PATH = "/Image/SwImageDesc.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(NewCreateVmCsar.class);

    /**
     * create vm csar.
     *
     * @param projectPath path of project
     * @param config vm create config of project
     * @param project project self
     * @return package gz
     */
    public File create(String projectPath, VmCreateConfig config, ApplicationProject project)
        throws IOException {
        File projectDir = new File(projectPath);

        String deployType = (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) ? "container" : "vm";
        String projectName = project.getName();
        String chartName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();

        // copy template files to the new project path
        File csar = DeveloperFileUtils
            .copyDirAndReName(new File(WORKSPACE_CSAR_PATH), projectDir, config.getAppInstanceId());

        // get data to Map<String, String>
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStamp = time.format(new Date());

        // modify the csar files and fill in the data
        try {
            File csarValue = new File(csar.getCanonicalPath() + simpleFiles);

            FileUtils.writeStringToFile(csarValue,
                FileUtils.readFileToString(csarValue, StandardCharsets.UTF_8).replace("{name}", projectName)
                    .replace("{provider}", project.getProvider()).replace("{version}", project.getVersion())
                    .replace("{time}", timeStamp).replace("{description}", project.getDescription())
                    .replace("{ChartName}", chartName).replace("{type}", deployType), StandardCharsets.UTF_8, false);
            csarValue.renameTo(new File(csar.getCanonicalPath() + "/" + projectName + ".mf"));

        } catch (IOException e) {
            throw new IOException("replace file exception");
        }
        //update vm config data

        String mainServiceTemplatePath = csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH;
        File templateFile = new File(mainServiceTemplatePath);
        String yamlContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
        yamlContent = yamlContent.replaceAll("\t", "");
        Yaml yaml = new Yaml();
        Map<String, Object> loaded = yaml.load(yamlContent);

        // config vm name
        LinkedHashMap<String, Object> virtualName = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","properties");
        virtualName.put("name", config.getVmName());
        // config vm memory
        LinkedHashMap<String, Object> virtualMemory = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","capabilities","virtual_compute", "properties", "virtual_memory");
        virtualMemory.put("virtual_mem_size", config.getVmRegulationDesc().getMemory()*1024);
        // config vm cpu
        LinkedHashMap<String, Object> virtualCpu = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","capabilities","virtual_compute", "properties", "virtual_cpu");
        virtualCpu.put("num_virtual_cpu", config.getVmRegulationDesc().getCpu());
        // config vm cpu_architecture
        virtualCpu.put("cpu_architecture", config.getVmRegulationDesc().getArchitecture());
        // config vm data storage
        LinkedHashMap<String, Object> virtualStorage = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","capabilities","virtual_compute", "properties", "virtual_local_storage");
        virtualStorage.put("size_of_storage", config.getVmRegulationDesc().getDataDisk());
        // config vm image data
        String imageData = config.getVmSystemDesc().getOperateSystem() + "_" + config.getVmSystemDesc().getVersion() +
            "_" + config.getVmSystemDesc().getSystemBit() + "_" + config.getVmSystemDesc().getSystemDisk() + "GB";
        LinkedHashMap<String, Object> virtualImage = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","properties","sw_image_data");
        virtualImage.put("name", imageData);

        // config flavor
        LinkedHashMap<String, Object> virtualFlavor = getObjectFromMap(loaded, "topology_template",
            "node_templates", "EMS_VDU1","properties","vdu_profile", "flavor_extra_specs");
        if(config.getVmRegulationDesc().getArchitecture().equals("X86")) {

            virtualFlavor.remove("mgmt_egarm", "true");
        }else {
            virtualFlavor.remove("mgmt_egx86", "true");
        }

        // config vm network type Network_Internet

        if (!config.getVmNetworkDesc().contains("Network_MEP")) {
            LinkedHashMap<String, Object> virtualNetwork = getObjectFromMap(loaded, "topology_template",
                "node_templates");
            virtualNetwork.remove("EMS_VDU1_CP0");
            virtualNetwork.remove("MEC_APP_MP1");
        }
        if (!config.getVmNetworkDesc().contains("Network_N6")) {
            LinkedHashMap<String, Object> virtualNetwork = getObjectFromMap(loaded, "topology_template",
                "node_templates");
            virtualNetwork.remove("EMS_VDU1_CP1");
            virtualNetwork.remove("MEC_APP_N6");
        }
        if (!config.getVmNetworkDesc().contains("Network_Internet")) {
            LinkedHashMap<String, Object> virtualNetwork = getObjectFromMap(loaded, "topology_template",
                "node_templates");
            virtualNetwork.remove("EMS_VDU1_CP2");
            virtualNetwork.remove("MEC_APP_INTERNET");
        }

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(templateFile, loaded);
        // delete ""
        File templateFileModify = new File(mainServiceTemplatePath);
        String yamlContents = FileUtils.readFileToString(templateFileModify, StandardCharsets.UTF_8);
        yamlContents = yamlContents.replaceAll("\"", "");
        writeFile(templateFileModify,yamlContents);

        //update SwImageDesc.json
        File imageJson = new File(csar.getCanonicalPath() + IMAGE_BASE_PATH);
        //fill  imageJson data
        Gson gson = new Gson();
        List<ImageDesc> imageDescs = new ArrayList<>();
        ImageDesc imageDesc = new ImageDesc();
        imageDesc.setId(UUID.randomUUID().toString());
        imageDesc.setName(imageData);
        imageDesc.setVersion(project.getVersion());
        imageDesc.setChecksum("2");
        imageDesc.setContainerFormat("bare");
        imageDesc.setDiskFormat("raw");
        imageDesc.setMinDisk(3);
        imageDesc.setMinRam(6);
        imageDesc.setArchitecture(project.getPlatform().get(0));
        imageDesc.setSize(688390);
        imageDesc.setSwImage("Image/" + imageData);
        imageDesc.setHw_scsi_model("virtio-scsi");
        imageDesc.setHw_disk_bus("scsi");
        imageDesc.setOperatingSystem("linux");
        imageDesc.setSupportedVirtualisationEnvironment("linux");
        imageDescs.add(imageDesc);
        // write data into imageJson file
        writeFile(imageJson, gson.toJson(imageDescs));

        return csar;
    }

    private void writeFile(File file, String content) {
        try {
            FileWriter fw = new FileWriter(file.getCanonicalPath());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

    public static LinkedHashMap<String, Object> getObjectFromMap(Map<String, Object> loaded, String... keys) {
        LinkedHashMap<String, Object> result = null;
        for (String key : keys) {
            result = (LinkedHashMap<String, Object>)loaded.get(key);
            if (result != null) {
                loaded = result;
            }
        }
        return result;
    }



}
