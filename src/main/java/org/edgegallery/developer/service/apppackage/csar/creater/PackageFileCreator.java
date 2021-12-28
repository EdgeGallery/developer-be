/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.service.apppackage.csar.creater;

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
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.apppackage.IToscaContentEnum;
import org.edgegallery.developer.model.apppackage.basiccontext.ManifestFiledataContent;
import org.edgegallery.developer.model.apppackage.basiccontext.ManifestMetadataContent;
import org.edgegallery.developer.model.apppackage.basiccontext.ToscaMetadataContent;
import org.edgegallery.developer.model.apppackage.basiccontext.ToscaSourceContent;
import org.edgegallery.developer.model.apppackage.basiccontext.VnfdToscaMetaContent;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.apppackage.csar.filehandler.IACsarFile;
import org.edgegallery.developer.service.apppackage.csar.filehandler.IContentParseHandler;
import org.edgegallery.developer.service.apppackage.csar.filehandler.TocsarFileHandlerFactory;
import org.edgegallery.developer.service.apppackage.csar.signature.EncryptedService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.util.ApplicationUtil;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Files;

public class PackageFileCreator {

    public static final Logger LOGGER = LoggerFactory.getLogger(PackageFileCreator.class);

    private static final String PACKAGE_TEMPLATE_PATH = "./configs/template/package_template";

    private static final String TEMPLATE_PACKAGE_VNFD__PATH = "/APPD/TOSCA_VNFD.meta";

    private static final String TEMPLATE_PACKAGE_METADATA_PATH = "/TOSCA-Metadata/TOSCA.meta";

    private static final String TEMPLATE_PACKAGE_DOCS_PATH = "/Artifacts/Docs/";

    private static final String TEMPLATE_APPD = "APPD/";

    private static final String TEMPLATE_PATH = "temp";

    private static final String TEMPLATE_DEFINITION = "Definition/";

    private static final String APPD_BASE_PATH = "/APPD/Definition/";

    private static final String APPD_FILE_TYPE = ".yaml";

    EncryptedService encryptedService = (EncryptedService) SpringContextUtil.getBean(EncryptedService.class);

    UploadFileService uploadService = (UploadFileService) SpringContextUtil.getBean(UploadFileService.class);

    private Application application;

    private String packageId;

    public String getPackagePath() {
        return ApplicationUtil.getPackageBasePath(application.getId(), packageId);
    }

    public String getAppdFilePath() {
        return getPackagePath() + APPD_BASE_PATH + getAppFileName(APPD_FILE_TYPE);
    }

    public String getApplicationPath() {
        return ApplicationUtil.getApplicationBasePath(application.getId());
    }

    public PackageFileCreator(Application application, String packageId) {
        if (application == null || StringUtils.isEmpty(packageId)) {
            LOGGER.error("application or packageIde is null");
            throw new EntityNotFoundException("application or packageIde is null", ResponseConsts.RET_FILE_NOT_FOUND);
        }
        this.application = application;
        this.packageId = packageId;
    }

    public boolean copyPackageTemplateFile() {
        File packageFileDir = new File(getPackagePath());
        if (!packageFileDir.exists() || !packageFileDir.isDirectory()) {
            File applicationDir = new File(getApplicationPath());
            try {
                DeveloperFileUtils.copyDirectory(new File(PACKAGE_TEMPLATE_PATH), applicationDir, packageId);
            } catch (IOException e) {
                LOGGER.error("copy package template file fail, package dir:{}", e.getMessage());
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
        contentSourceMap.put(ManifestFiledataContent.SOURCE, TEMPLATE_APPD + getAppFileName(Consts.FILE_FORMAT_ZIP));
        writeFile(mfFile, mfFileHandler.toString());
        boolean result = mfFile
            .renameTo(new File(getPackagePath() + File.separator + getAppFileName(Consts.FILE_FORMAT_MF)));
        if (!result) {
            LOGGER.warn("file name modify fail:{}", getAppFileName(Consts.FILE_FORMAT_MF));
        }
    }

    /**
     * modify file: /TOSCA-Metadata/TOSCA.meta.
     */
    public void configMetaFile() {
        File metaFile = new File(getPackagePath() + TEMPLATE_PACKAGE_METADATA_PATH);
        IACsarFile metaFileHandler = TocsarFileHandlerFactory
            .createFileHandler(TocsarFileHandlerFactory.TOSCA_META_FILE);
        metaFileHandler.load(metaFile);
        IContentParseHandler content = metaFileHandler.getParamsHandlerList().get(0);
        Map<IToscaContentEnum, String> contentMap = content.getParams();
        contentMap.put(ToscaMetadataContent.ENTRY_DEFINITIONS, TEMPLATE_APPD + getAppFileName(Consts.FILE_FORMAT_ZIP));

        IContentParseHandler contentName = metaFileHandler.getParamsHandlerList().get(1);
        Map<IToscaContentEnum, String> contentNameMap = contentName.getParams();
        contentNameMap.put(ToscaSourceContent.NAME, TEMPLATE_APPD + getAppFileName(Consts.FILE_FORMAT_ZIP));
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
        IContentParseHandler content = metaFileHandler.getParamsHandlerList().get(0);
        Map<IToscaContentEnum, String> contentMap = content.getParams();
        contentMap
            .put(VnfdToscaMetaContent.ENTRY_DEFINITIONS, TEMPLATE_DEFINITION + getAppFileName(Consts.FILE_FORMAT_YAML));

        IContentParseHandler contentName = metaFileHandler.getParamsHandlerList().get(1);
        Map<IToscaContentEnum, String> contentNameMap = contentName.getParams();
        contentNameMap.put(ToscaSourceContent.NAME, TEMPLATE_DEFINITION + getAppFileName(Consts.FILE_FORMAT_YAML));
        writeFile(metaFile, metaFileHandler.toString());
    }

    /**
     * copy md and icon file: /Artifacts/Docs.
     */
    public void configMdAndIcon() {
        // move icon file
        DeveloperFileUtils.clearFiles(getPackagePath() + TEMPLATE_PACKAGE_DOCS_PATH);
        try {

            // move icon file to package
            UploadFile iconFile = uploadService.getFile(application.getIconFileId());
            DeveloperFileUtils.copyFile(new File(InitConfigUtil.getWorkSpaceBaseDir() + iconFile.getFilePath()),
                new File(getPackagePath() + TEMPLATE_PACKAGE_DOCS_PATH + iconFile.getFileName()));
        } catch (IOException e) {
            LOGGER.warn("copy icon file fail:{}", e.getMessage());
        }
        // move des md file to package
        if (StringUtils.isEmpty(application.getGuideFileId())) {
            return;
        }
        UploadFile mdFile = uploadService.getFile(application.getGuideFileId());
        try {
            DeveloperFileUtils.copyFile(new File(InitConfigUtil.getWorkSpaceBaseDir() + mdFile.getFilePath()),
                new File(getPackagePath() + TEMPLATE_PACKAGE_DOCS_PATH + mdFile.getFileName()));
        } catch (IOException e) {
            LOGGER.warn("copy icon file fail:{}", e.getMessage());
        }
    }

    public String packageFileCompress() {
        File packageFileDir = new File(getPackagePath());
        LOGGER.info("packageFileDir:{}", packageFileDir.getPath());
        if (!packageFileDir.exists() || !packageFileDir.isDirectory()) {
            LOGGER.error("package file does not exist");
            return null;
        }
        String tempPackagePath = getPackagePath() + TEMPLATE_PATH;
        String tempPackageName = packageId + TEMPLATE_PATH;
        try {
            DeveloperFileUtils.copyDirectory(packageFileDir, new File(getApplicationPath()), tempPackageName);
            // compress appd
            String appdDir = tempPackagePath + File.separator + "APPD";
            CompressFileUtils.fileToZip(appdDir, getAppFileName(""));
            // compress helm chart
            compressDeploymentFile();
            boolean encryptedResult = encryptedService.encryptedCMS(tempPackagePath);
            if (!encryptedResult) {
                LOGGER.error("sign package failed");
                return null;
            }
            // compress package
            CompressFileUtilsJava.compressToCsarAndDeleteSrc(tempPackagePath, getApplicationPath(), packageId);
        } catch (IOException e) {
            LOGGER.error("package compress fail, package path:{}", tempPackagePath);
            return null;
        }

        return packageId + Consts.FILE_FORMAT_CSAR;
    }

    public void generateImageDesFile() {
        // container app need override
    }

    public boolean compressDeploymentFile() {
        return true;
    }

    protected String getAppFileName(String format) {
        return application.getName() + "_" + application.getProvider() + "_" + application.getVersion() + "_"
            + application.getArchitecture() + format;
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
    public void writeFile(File file, String content) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(content);
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

}
