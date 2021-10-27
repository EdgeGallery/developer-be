package org.edgegallery.developer.service.apppackage.csar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.apppackage.IToscaContentEnum;
import org.edgegallery.developer.model.apppackage.basicContext.ManifestFiledataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ManifestMetadataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ToscaMetadataContent;
import org.edgegallery.developer.model.apppackage.basicContext.ToscaSourceContent;
import org.edgegallery.developer.model.apppackage.basicContext.VnfdToscaMetaContent;
import org.edgegallery.developer.service.apppackage.csar.impl.TocsarFileHandlerFactory;
import org.edgegallery.developer.service.apppackage.signature.EncryptedService;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.SpringContextUtil;
import org.edgegallery.developer.util.applicationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Files;

public class PackageFileCreator {

    public static final Logger LOGGER = LoggerFactory.getLogger(PackageFileCreator.class);

    private static final String PACKAGE_TEMPLATE_PATH = "template/package_template";

    private static final String TEMPLATE_PACKAGE_VNFD__PATH = "/APPD/TOSCA_VNFD.meta";

    private static final String TEMPLATE_PACKAGE_METADATA_PATH = "/TOSCA-Metadata/TOSCA.meta";

    private static final String TEMPLATE_PACKAGE_HELM_CHART_PATH = "/Artifacts/Deployment/Charts/";

    private static final String TEMPLATE_APPD = "APPD/";

    private static final String TEMPLATE_PATH = "temp";

    EncryptedService encryptedService = (EncryptedService) SpringContextUtil.getBean(EncryptedService.class);

    private Application application;

    private String packageId;

    private String helmChartName;

    public String getPackagePath() {
        return applicationUtil.getPackageBasePath(application.getId(), packageId);
    }

    public String getApplicationPath() {
        return applicationUtil.getApplicationBasePath(application.getId());
    }

    public PackageFileCreator(Application application, String packageId) {
        if (application == null || StringUtils.isEmpty(packageId)) {
            LOGGER.error("application or packageIde is null");
            throw new EntityNotFoundException("application or packageIde is null", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        this.application = application;
        this.packageId = packageId;
    }

    public PackageFileCreator(Application application, String packageId, String helmChartName) {
        if (application == null || StringUtils.isEmpty(packageId) || StringUtils.isEmpty(helmChartName)) {
            LOGGER.error("application or packageIde or helmChartName is null");
            throw new EntityNotFoundException("application or packageIde or helmChartName is null",
                ResponseConsts.RET_FILE_NOT_FOUND);
        }
        this.application = application;
        this.packageId = packageId;
        this.helmChartName = helmChartName;
    }

    public boolean copyPackageTemplateFile() {
        File packageFileDir = new File(getPackagePath());
        if (!packageFileDir.exists() || !packageFileDir.isDirectory()) {
            File applicationDir = new File(getApplicationPath());
            try {
                DeveloperFileUtils
                    .copyDirectory(Resources.getResourceAsFile(PACKAGE_TEMPLATE_PATH), applicationDir, packageId);
            } catch (IOException e) {
                LOGGER.error("copy package template file fail, package dir:{}", getPackagePath());
                return false;
            }

        }
        return true;

    }

    /**
     * modify file: mf.
     */
    public void configMfFile() {
        File mfFile = getFile(getPackagePath(), "mf");
        IACsarFile mfFileHandler = TocsarFileHandlerFactory.createFileHandler(TocsarFileHandlerFactory.MF_FILE);
        mfFileHandler.load(mfFile);
        IContentParseHandler content = mfFileHandler.getContentByTypeAndValue(ManifestMetadataContent.METADATA, "");
        Map<IToscaContentEnum, String> contentMap = content.getParams();
        String appType = "EdgeGallery_" + application.getAppClass().toString() + "_package";
        contentMap.put(ManifestMetadataContent.APP_PRODUCT_NAME, application.getName());
        contentMap.put(ManifestMetadataContent.APP_PROVIDER_ID, application.getProvider());
        contentMap.put(ManifestMetadataContent.APP_PACKAGE_VERSION, application.getVersion());
        contentMap.put(ManifestMetadataContent.APP_RELEASE_DATA_TIME, application.getCreateTime());
        contentMap.put(ManifestMetadataContent.APP_TYPE, appType);
        contentMap.put(ManifestMetadataContent.APP_CLASS, application.getAppClass().toString().toLowerCase());
        contentMap.put(ManifestMetadataContent.APP_PACKAGE_DESCRIPTION, application.getDescription());
        IContentParseHandler contentSource = mfFileHandler
            .getContentByTypeAndValue(ManifestFiledataContent.SOURCE, TEMPLATE_APPD);
        Map<IToscaContentEnum, String> contentSourceMap = contentSource.getParams();
        contentSourceMap.put(ManifestFiledataContent.SOURCE, TEMPLATE_APPD + appdName(".zip"));
        writeFile(mfFile, mfFileHandler.toString());
        mfFile.renameTo(new File(getPackagePath() + "/" + appdName(".mf")));
    }

    /**
     * modify file: /TOSCA-Metadata/TOSCA.meta.
     */
    public void configMetaFile() {
        File metaFile = new File(getPackagePath() + TEMPLATE_PACKAGE_METADATA_PATH);
        IACsarFile metaFileHandler = TocsarFileHandlerFactory
            .createFileHandler(TocsarFileHandlerFactory.TOSCA_META_FILE);
        metaFileHandler.load(metaFile);
        IContentParseHandler content = metaFileHandler
            .getContentByTypeAndValue(ToscaMetadataContent.TOSCA_META_FILE_VERSION, "");
        Map<IToscaContentEnum, String> contentMap = content.getParams();
        contentMap.put(ToscaMetadataContent.ENTRY_DEFINITIONS, TEMPLATE_APPD + appdName(".zip"));

        IContentParseHandler contentName = metaFileHandler
            .getContentByTypeAndValue(ToscaSourceContent.NAME, application.getAppClass().toString().toLowerCase());
        Map<IToscaContentEnum, String> contentNameMap = contentName.getParams();
        contentNameMap.put(ToscaSourceContent.NAME, TEMPLATE_APPD + appdName(".zip"));
        writeFile(metaFile, metaFileHandler.toString());
    }

    /**
     * modify file: /APPD/TOSCA_VNFD.meta.
     */
    public void configVnfdMeta() {
        File metaFile = new File(getPackagePath() + TEMPLATE_PACKAGE_VNFD__PATH);
        IACsarFile metaFileHandler = TocsarFileHandlerFactory
            .createFileHandler(TocsarFileHandlerFactory.VNFD_META_FILE);
        metaFileHandler.load(metaFile);
        IContentParseHandler content = metaFileHandler
            .getContentByTypeAndValue(VnfdToscaMetaContent.VNFD_META_FILE_VERSION, "");
        Map<IToscaContentEnum, String> contentMap = content.getParams();
        contentMap.put(VnfdToscaMetaContent.ENTRY_DEFINITIONS, "Definition/" + appdName(".yaml"));

        IContentParseHandler contentName = metaFileHandler.getContentByTypeAndValue(ToscaSourceContent.NAME,
            application.getAppClass().toString().toLowerCase());
        Map<IToscaContentEnum, String> contentNameMap = contentName.getParams();
        contentNameMap.put(ToscaSourceContent.NAME, "Definition/" + appdName(".yaml"));
        writeFile(metaFile, metaFileHandler.toString());
    }

    public String PackageFileCompress() {
        File packageFileDir = new File(getPackagePath());
        if (!packageFileDir.exists() || !packageFileDir.isDirectory()) {
            LOGGER.error("package file is not exited");
            return null;
        }
        String tempPackagePath = getPackagePath() + TEMPLATE_PATH;
        String tempPackageName = packageId + TEMPLATE_PATH;
        try {
            DeveloperFileUtils.copyDirectory(packageFileDir, new File(getApplicationPath()), tempPackageName);
            // compress appd
            String appdDir = tempPackagePath + File.separator + "APPD";
            CompressFileUtils.fileToZip(appdDir, appdName(""));
            // compress helm chart
            compressHemChartFile();
            encryptedService.encryptedFile(tempPackagePath);
            encryptedService.encryptedCMS(tempPackagePath);
            // compress package
            CompressFileUtilsJava
                .compressToCsarAndDeleteSrc(tempPackagePath, getApplicationPath(), packageId);
        } catch (IOException e) {
            LOGGER.error("package compress fail, package path:{}", tempPackagePath);
            return null;
        }

        return getPackagePath() + ".zip";
    }

    public boolean compressHemChartFile() {
        return true;
    }

    private String appdName(String format) {
        return application.getName() + "_" + application.getProvider() + "_" + application.getVersion()
            + "_" + application.getArchitecture() + "_" + application.getAppClass().toString().toLowerCase() + format;
    }


    /**
     * get file by parent directory and file extension.
     */
    public File getFile(String parentDir, String fileExtension) {
        List<File> files = (List<File>) FileUtils.listFiles(new File(parentDir), null, true);
        for (File fileEntry : files) {
            if (Files.getFileExtension(fileEntry.getName().toLowerCase(Locale.ROOT)).equals(fileExtension)) {
                return fileEntry;
            }
        }
        return null;
    }

    /**
     * write json file.
     *
     * @param file    file.
     * @param content content.
     */
    private void writeFile(File file, String content) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(content);
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

}
