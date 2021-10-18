package org.edgegallery.developer.service.apppackage.scar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.apppackage.appd.APPDDefinition;
import org.edgegallery.developer.util.APPDParserUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;


public class VMPackageFileCreator {

    private static final String APPD_TEMPLATE_PATH = "template/appd/vm_appd_template.yaml";

    private static final String PACKAGE_BASE_PATH = "APPD";


    private VMApplication application;

    private String packageId;

    public VMPackageFileCreator(VMApplication application, String packageId) {
        this.application = application;
        this.packageId = packageId;

    }


    public void generatePackageFile() {

        String packagePath = getPackageBasePath();


    }

    private File generateAPPDYaml() throws IOException {
        File vmAPPDTemplate = new File(APPD_TEMPLATE_PATH);
        String yamlContent = FileUtils.readFileToString(vmAPPDTemplate, StandardCharsets.UTF_8);
        APPDDefinition definition = APPDParserUtil.parseAppd(yamlContent);

        return new File(getPackageBasePath());
    }

    private File generateImageDesFile()  {

        return new File(getPackageBasePath());

    }

    private String getPackageBasePath() {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + application.getId() +
            File.separator + packageId;
    }
}
