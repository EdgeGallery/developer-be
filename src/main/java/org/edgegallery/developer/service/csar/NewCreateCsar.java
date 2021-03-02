package org.edgegallery.developer.service.csar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.deployyaml.PodImage;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class NewCreateCsar {

    private static final String simpleFiles = "/positioning-service.mf";

    private static final String WORKSPACE_CSAR_PATH = "./configs/new_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/Artifacts/Deployment/Charts/";

    private static final String IMAGE_BASE_PATH = "Image/SwImageDesc.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(NewCreateCsar.class);

    /**
     * create csar.
     *
     * @param projectPath path of project
     * @param config test config of project
     * @param project project self
     * @return package gz
     */
    public File create(String projectPath, ProjectTestConfig config, ApplicationProject project, File chart)
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

        if (chart != null) {
            // move chart.tgz to Chart directory and delete apptgz dir
            File chartDir = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH);
            FileUtils.cleanDirectory(chartDir);
            FileUtils.moveFileToDirectory(chart, chartDir, true);
        } else {
            //compose apptgz to .tgz and delete apptgz dir
            String appName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();
            File appTgz = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + "app-tgz/");
            File appTgzNew = new File(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, appName);
            if (!appTgz.renameTo(appTgzNew)) {
                throw new IOException("Rename tgz exception");
            }
            File tgz = CompressFileUtils
                .compressToTgzAndDeleteSrc(csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH + appName,
                    csar.getCanonicalPath() + TEMPLATE_CSAR_BASE_PATH, appName);
            if (!tgz.exists()) {
                throw new IOException("Create tgz exception");
            }
        }
        //update SwImageDesc.json
        File imageJson = new File(csar.getCanonicalPath() + File.separator + IMAGE_BASE_PATH);
        //query saved pod data
        String projectId = project.getId();
        List<ProjectImageConfig> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(projectId)) {
            list = ImageUtils.getAllImage(projectId);
        }
        if (!CollectionUtils.isEmpty(list)) {
            ProjectImageConfig imageConfig = list.get(0);
            String containers = imageConfig.getPodContainers();
            List<PodImage> images = new Gson().fromJson(containers, new TypeToken<List<PodImage>>() { }.getType());
            String imageData = getSwImageData(images, project);
            // write data into imageJson file
            writeFile(imageJson, imageData);
        }
        return csar;
    }

    private String getSwImageData(List<PodImage> images, ApplicationProject project) {
        Gson gson = new Gson();
        List<ImageDesc> imageDescs = new ArrayList<>();
        for (PodImage obj : images) {
            String[] podImages = obj.getPodImage();
            for (String pod : podImages) {
                ImageDesc imageDesc = new ImageDesc();
                imageDesc.setId(UUID.randomUUID().toString());
                String[] vers = pod.split(":");
                imageDesc.setName(vers[0]);
                imageDesc.setVersion(vers[1]);
                imageDesc.setChecksum("2");
                imageDesc.setContainerFormat("bare");
                imageDesc.setDiskFormat("raw");
                imageDesc.setMinDisk(3);
                imageDesc.setMinRam(6);
                imageDesc.setArchitecture(project.getPlatform().get(0));
                imageDesc.setSize(688390);
                String env = "\\{\\{.Values.imagelocation.domainname}}/\\{\\{.Values.imagelocation.project}}";
                String envs = StringEscapeUtils.unescapeJava(env);
                if (pod.contains(envs)) {
                    pod = pod.replace(envs, ImageConfig.getDomains() + "/" + ImageConfig.getProjects());
                }
                imageDesc.setSwImage(pod);
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
