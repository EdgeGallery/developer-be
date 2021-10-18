package org.edgegallery.developer.service.apppackage.scar;

import java.io.File;
import java.io.IOException;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;

public class ContainerPackageFileCreator {

    private ContainerApplication application;

    private String packageId;

    public ContainerPackageFileCreator(ContainerApplication application, String packageId) {
        this.application = application;
        this.packageId = packageId;

    }

    private File generateAPPDYaml()  {

        return new File(getPackageBasePath());

    }

    private File generateHelmChart()  {

        return new File(getPackageBasePath());

    }

    private File generateImageDesFile()  {

        return new File(getPackageBasePath());

    }

    public File generatePackageFile() {
        return new File(getPackageBasePath());
    }

    private String getPackageBasePath() {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + application.getId() + File.separator;
    }


}
