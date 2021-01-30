package org.edgegallery.developer.service.csar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.stringtemplate.v4.ST;

public class NewCreateCsar {

    private static final String simpleFiles = "/positioning-service.mf";

    private static final String WORKSPACE_CSAR_PATH = "./configs/new_csar";

    private static final String TEMPLATE_CSAR_BASE_PATH = "/Artifacts/Deployment/Charts/";

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

        String deployType = (project.getDeployPlatform() == EnumDeployPlatform.KUBERNETES)?"container":"vm";


        // copy template files to the new project path
        File csar = DeveloperFileUtils
            .copyDirAndReName(new File(WORKSPACE_CSAR_PATH), projectDir, config.getAppInstanceId());

        // get data to Map<String, String>
        String timeStamp = String.valueOf(new Date().getTime());

        // modify the csar files and fill in the data
        try {
            File csarValue = new File(csar.getCanonicalPath() + simpleFiles);
            String projectName = project.getName();
            String chartName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();
            FileUtils.writeStringToFile(csarValue,
                FileUtils.readFileToString(csarValue, StandardCharsets.UTF_8).replace("{name}", projectName)
                    .replace("{provider}", project.getProvider()).replace("{version}", project.getVersion())
                    .replace("{time}", timeStamp).replace("{description}", project.getDescription())
                    .replace("{ChartName}", chartName).replace("{type}", deployType),
                StandardCharsets.UTF_8, false);

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
            String appName = project.getName() + timeStamp;
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

        return csar;
    }

}
