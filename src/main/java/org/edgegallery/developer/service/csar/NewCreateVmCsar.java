package org.edgegallery.developer.service.csar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;

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
        JsonNode jsonNodeTree = new ObjectMapper().readTree(config.getTemplateJson());
        // save it as YAML
        String jsonAsYaml = new YAMLMapper().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
            .configure(YAMLGenerator.Feature.INDENT_ARRAYS, true).writeValueAsString(jsonNodeTree);
        File configYaml = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH);
        FileUtils.writeStringToFile(configYaml,jsonAsYaml,"UTF-8");

        return csar;
    }


    private String getSwImageData(String[] conArr, ApplicationProject project) {
        Gson gson = new Gson();
        List<ImageDesc> imageDescs = new ArrayList<>();
        for (String obj : conArr) {
            if (obj.startsWith("\"image\":")) {
                ImageDesc imageDesc = new ImageDesc();
                imageDesc.setId(UUID.randomUUID().toString());
                String[] images = obj.split(":");
                String image = (images[1] + ":" + images[2]).replaceAll("\"", "");
                String[] imageNames = image.split("/");
                String imageName = imageNames[2].replaceAll("\"", "");
                String[] imageVersions = imageName.split(":");
                imageDesc.setName(imageName);
                // String version = image.split(":")[];
                imageDesc.setVersion(imageVersions[1]);
                imageDesc.setChecksum("2");
                imageDesc.setContainerFormat("bare");
                imageDesc.setDiskFormat("raw");
                imageDesc.setMinDisk(3);
                imageDesc.setMinRam(6);
                imageDesc.setArchitecture(project.getPlatform().get(0));
                imageDesc.setSize(688390);
                imageDesc.setSwImage(image);
                imageDesc.setHw_scsi_model("virtio-scsi");
                imageDesc.setHw_disk_bus("scsi");
                imageDesc.setOperatingSystem("linux");
                imageDesc.setSupportedVirtualisationEnvironment("linux");
                imageDescs.add(imageDesc);
            }
        }
        return gson.toJson(imageDescs);
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

}
