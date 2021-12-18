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

package org.edgegallery.developer.service.application.impl.vm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.mapper.application.vm.VMInstantiateInfoMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.constant.AppdConstants;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.EnumImageExportStatus;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.resource.pkgspec.PkgSpec;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.reverseproxy.ScpConnectEntity;
import org.edgegallery.developer.model.uploadfile.FileUploadEntity;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.vm.VMExportImageOperation;
import org.edgegallery.developer.service.application.action.impl.vm.VMLaunchOperation;
import org.edgegallery.developer.service.application.impl.AppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.service.recource.pkgspec.PkgSpecService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.ShhFileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VMAppOperationServiceImpl extends AppOperationServiceImpl implements VMAppOperationService {

    public static final String OPERATION_LAUNCH_NAME = "VirtualMachine launch";

    public static final String OPERATION_EXPORT_IMAGE_NAME = "VirtualMachine Export Image";

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppOperationServiceImpl.class);

    @Value("${upload.tempPath}")
    private String tempUploadPath;

    private static final String SUBDIR_FILE = "uploadFile";

    @Autowired
    private VMInstantiateInfoMapper vmInstantiateInfoMapper;

    @Autowired
    private ImageExportInfoMapper imageExportInfoMapper;

    @Autowired
    private OperationStatusService operationStatusService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private VMAppVmServiceImpl vmAppVmServiceImpl;

    @Autowired
    private AppPackageService appPackageService;

    @Autowired
    private MepHostService mepHostService;

    @Autowired
    private PkgSpecService pkgSpecService;

    @Override
    public OperationInfoRep instantiateVM(String applicationId, String vmId, User user) {

        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application does not exist,id:{}", applicationId);
            throw new EntityNotFoundException("application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        VirtualMachine virtualMachine = vmAppVmServiceImpl.getVm(applicationId, vmId);
        if (virtualMachine == null) {
            LOGGER.error("instantiate vm app fail ,vm does not exit,vmId:{}", vmId);
            throw new EntityNotFoundException("instantiate vm app fail ,vm does not exit.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        if (virtualMachine.getVmInstantiateInfo() != null) {
            LOGGER.error("instantiate vm app fail ,vm has instantiated. please clean env, vmId:{}", vmId);
            throw new IllegalRequestException("instantiate vm app fail ,vm has instantiated. please clean env.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(user.getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.APPLICATION_INSTANCE);
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setObjectId(vmId);
        operationStatus.setObjectName(virtualMachine.getName());
        operationStatus.setOperationName(OPERATION_LAUNCH_NAME);
        operationStatusService.createOperationStatus(operationStatus);
        VMLaunchOperation actionCollection = new VMLaunchOperation(user, applicationId, vmId, operationStatus);
        VMInstantiateInfo instantiateInfo = new VMInstantiateInfo();
        instantiateInfo.setOperationId(operationStatus.getId());
        createInstantiateInfo(vmId, instantiateInfo);
        LOGGER.info("start instantiate vm app");
        new InstantiateVmAppProcessor(operationStatusService, operationStatus, actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public Boolean uploadFileToVm(String applicationId, String vmId, HttpServletRequest request, Chunk chunk) {
        LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
            chunk.getIdentifier(), chunk.getChunkNumber());
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            LOGGER.error("upload request is invalid.");
            throw new IllegalRequestException("upload request is invalid.", ResponseConsts.RET_UPLOAD_FILE_FAIL);
        }

        MultipartFile file = chunk.getFile();
        if (file == null) {
            LOGGER.error("can not find any needed file");
            throw new IllegalRequestException("can not find any needed file.", ResponseConsts.RET_UPLOAD_FILE_FAIL);
        }

        File tmpUploadDir = new File(getUploadFileRootDir());
        if (!tmpUploadDir.exists()) {
            boolean isMk = tmpUploadDir.mkdirs();
            if (!isMk) {
                LOGGER.error("create temporary upload path failed");
                throw new FileOperateException("create temporary upload path failed",
                    ResponseConsts.RET_CREATE_FILE_FAIL);
            }
        }

        Integer chunkNumber = chunk.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }
        File outFile = new File(getUploadFileRootDir() + chunk.getIdentifier(), chunkNumber + ".part");
        try {
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        } catch (IOException e) {
            LOGGER.error("save temporary file failed");
            throw new FileOperateException("save temporary file failed", ResponseConsts.RET_CREATE_FILE_FAIL);
        }

        return true;
    }

    @Override
    public Boolean mergeAppFile(String applicationId, String vmId, String fileName, String identifier) {
        LOGGER.info("start merge file,fileName:{}", fileName);
        String partFilePath = getUploadFileRootDir() + identifier;
        File partFileDir = new File(partFilePath);
        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("Uploaded chunk file can not be found for file {}", fileName);
            throw new IllegalRequestException("Can not find any chunk file to merge.",
                ResponseConsts.RET_MERGE_FILE_FAIL);
        }

        File mergedFile = new File(getUploadFileRootDir() + File.separator + fileName);
        try {
            FileOutputStream destTempfos = new FileOutputStream(mergedFile, true);
            for (int i = 1; i <= partFiles.length; i++) {
                File partFile = new File(partFilePath, i + ".part");
                FileUtils.copyFile(partFile, destTempfos);
            }
            destTempfos.close();
            FileUtils.deleteDirectory(partFileDir);

        } catch (IOException e) {
            LOGGER.error("Merge file failed");
            throw new FileOperateException("Merge file failed.", ResponseConsts.RET_MERGE_FILE_FAIL);
        }

        boolean pushFileToVmRes = pushFileToVm(mergedFile, applicationId, vmId);
        FileUtils.deleteQuietly(new File(getUploadFileRootDir()));

        if (!pushFileToVmRes) {
            LOGGER.error("Push file to VM failed!");
            throw new FileOperateException("Push file to VM failed.", ResponseConsts.RET_CREATE_FILE_FAIL);
        }
        return true;
    }

    @Override
    public OperationInfoRep createVmImage(String applicationId, String vmId, User user) {
        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application does not exist, id:{}", applicationId);
            throw new EntityNotFoundException("application does not exist.", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }

        VirtualMachine virtualMachine = vmAppVmServiceImpl.getVm(applicationId, vmId);
        if (virtualMachine == null || !virtualMachine.getVmInstantiateInfo().getStatus()
            .equals(EnumAppInstantiateStatus.SUCCESS)) {
            LOGGER.error("instantiate vm app not success  or vm does not exist , vmId:{}", vmId);
            throw new EntityNotFoundException("instantiate vm app not success  or vm does not exist.",
                ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        String appInstanceId = virtualMachine.getVmInstantiateInfo().getAppInstanceId();
        String vmInstanceId = virtualMachine.getVmInstantiateInfo().getVmInstanceId();

        // create OperationStatus
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setId(UUID.randomUUID().toString());
        operationStatus.setUserName(user.getUserName());
        operationStatus.setObjectType(EnumOperationObjectType.VM_IMAGE_INSTANCE);
        operationStatus.setStatus(EnumActionStatus.ONGOING);
        operationStatus.setProgress(0);
        operationStatus.setObjectId(vmId);
        operationStatus.setObjectName(virtualMachine.getName());
        operationStatus.setOperationName(OPERATION_EXPORT_IMAGE_NAME);
        operationStatusService.createOperationStatus(operationStatus);
        VMExportImageOperation actionCollection = new VMExportImageOperation(user, applicationId, vmId, operationStatus,
            appInstanceId, vmInstanceId);

        ImageExportInfo imageExportInfo = new ImageExportInfo();
        imageExportInfo.setOperationId(operationStatus.getId());
        imageExportInfo.setStatus(EnumImageExportStatus.IMAGE_CREATING);
        createExportInfo(vmId, imageExportInfo);
        LOGGER.info("start export vm image");
        new ExportVmImageProcessor(operationStatusService, operationStatus, actionCollection).start();
        return new OperationInfoRep(operationStatus.getId());
    }

    @Override
    public Boolean cleanEnv(String applicationId, User user) {
        Application application = applicationService.getApplication(applicationId);
        if (application == null) {
            LOGGER.error("application does not exist ,id:{}", applicationId);
            return true;
        }
        if (StringUtils.isEmpty(application.getMepHostId())) {
            return true;
        }
        List<VirtualMachine> vms = vmAppVmServiceImpl.getAllVm(applicationId);
        if (CollectionUtils.isEmpty(vms)) {
            LOGGER.error("vm does not exist in application, applicationId:{}", applicationId);
            applicationService.updateApplicationStatus(applicationId, EnumApplicationStatus.CREATED);
            return true;
        }
        for (VirtualMachine vm : vms) {
            cleanVmLaunchInfo(application.getMepHostId(), vm, user);

        }
        applicationService.updateApplicationStatus(applicationId, EnumApplicationStatus.CONFIGURED);
        return true;
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        ApplicationDetail detail = applicationService.getApplicationDetail(applicationId);
        return generatePackage(detail.getVmApp());
    }

    @Override
    public AppPackage generatePackage(VMApplication application) {
        return appPackageService.generateAppPackage(application);
    }

    public VMInstantiateInfo getInstantiateInfo(String vmId) {
        VMInstantiateInfo instantiateInfo = vmInstantiateInfoMapper.getVMInstantiateInfo(vmId);
        if (instantiateInfo != null) {
            List<PortInstantiateInfo> portLst = vmInstantiateInfoMapper.getPortInstantiateInfo(vmId);
            instantiateInfo.setPortInstanceList(portLst);
        }
        return instantiateInfo;
    }

    @Override
    public Boolean createInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo) {
        int res = vmInstantiateInfoMapper.createVMInstantiateInfo(vmId, instantiateInfo);
        if (res < 1) {
            LOGGER.error("create vm instantiate info failed");
            throw new DataBaseException("create vm instantiate info failed.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public void deleteInstantiateInfo(String vmId) {
        VMInstantiateInfo vmInstantiateInfo = getInstantiateInfo(vmId);
        if (vmInstantiateInfo == null) {
            return;
        }
        vmInstantiateInfoMapper.deletePortInstantiateInfo(vmId);
        vmInstantiateInfoMapper.deleteVMInstantiateInfo(vmId);
    }

    @Override
    public Boolean createExportInfo(String vmId, ImageExportInfo imageExportInfo) {
        if (imageExportInfoMapper.getImageExportInfoInfoByVMId(vmId) != null) {
            imageExportInfoMapper.deleteImageExportInfoInfoByVMId(vmId);
        }
        int res = imageExportInfoMapper.createImageExportInfoInfo(vmId, imageExportInfo);
        if (res < 1) {
            LOGGER.warn("create image export info baseDate fail");
            throw new DataBaseException("create image export info baseDate fail.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean modifyExportInfo(String vmId, ImageExportInfo imageExportInfo) {
        int res = imageExportInfoMapper.modifyImageExportInfoInfoByVMId(vmId, imageExportInfo);
        if (res < 1) {
            LOGGER.warn("modify image export info baseDate fail");
            throw new DataBaseException("modify image export info baseDate fail.", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean deleteExportInfo(String vmId) {
        imageExportInfoMapper.deleteImageExportInfoInfoByVMId(vmId);
        return true;
    }

    @Override
    public Boolean updateInstantiateInfo(String vmId, VMInstantiateInfo instantiateInfo) {
        int res = vmInstantiateInfoMapper.modifyVMInstantiateInfo(vmId, instantiateInfo);
        if (res < 1) {
            LOGGER.error("Update vm instantiate info failed");
            return false;
        }
        //update ports
        vmInstantiateInfoMapper.deletePortInstantiateInfo(vmId);
        if (CollectionUtils.isEmpty(instantiateInfo.getPortInstanceList())) {
            return true;
        }
        for (PortInstantiateInfo port : instantiateInfo.getPortInstanceList()) {
            res = vmInstantiateInfoMapper.createPortInstantiateInfo(vmId, port);
            if (res < 1) {
                LOGGER.error("Update vm instantiate info failed, add port instances failed.");
                return false;
            }
        }
        return true;
    }

    public ImageExportInfo getImageExportInfo(String vmId) {
        return imageExportInfoMapper.getImageExportInfoInfoByVMId(vmId);
    }

    public static class InstantiateVmAppProcessor extends Thread {

        OperationStatusService operationStatusService;

        OperationStatus operationStatus;

        VMLaunchOperation actionCollection;

        public InstantiateVmAppProcessor(OperationStatusService operationStatusService, OperationStatus operationStatus,
            VMLaunchOperation actionCollection) {
            this.operationStatusService = operationStatusService;
            this.operationStatus = operationStatus;
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            try {
                IActionIterator iterator = actionCollection.getActionIterator();
                while (iterator.hasNext()) {
                    IAction action = iterator.nextAction();
                    boolean result = action.execute();
                    if (!result) {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("InstantiateVmAppProcessor Exception.", e);
                operationStatus.setStatus(EnumActionStatus.FAILED);
                operationStatus.setErrorMsg("Exception happens when instantiate VM: " + e.getMessage());
                operationStatusService.modifyOperationStatus(operationStatus);
            }

        }

    }

    public static class ExportVmImageProcessor extends Thread {

        OperationStatusService operationStatusService;

        OperationStatus operationStatus;

        VMExportImageOperation actionCollection;

        public ExportVmImageProcessor(OperationStatusService operationStatusService, OperationStatus operationStatus,
            VMExportImageOperation actionCollection) {
            this.operationStatusService = operationStatusService;
            this.operationStatus = operationStatus;
            this.actionCollection = actionCollection;
        }

        @Override
        public void run() {
            try {
                IActionIterator iterator = actionCollection.getActionIterator();
                while (iterator.hasNext()) {
                    IAction action = iterator.nextAction();
                    boolean result = action.execute();
                    if (!result) {

                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("InstantiateVmAppProcessor Exception.", e);
                operationStatus.setStatus(EnumActionStatus.FAILED);
                operationStatus.setErrorMsg("Exception happens when export image: " + e.getMessage());
                operationStatusService.modifyOperationStatus(operationStatus);
            }
        }
    }

    private void cleanVmLaunchInfo(String mepHostId, VirtualMachine vm, User user) {
        MepHost mepHost = mepHostService.getHost(mepHostId);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        VMInstantiateInfo vmInstantiateInfo = vm.getVmInstantiateInfo();
        ImageExportInfo imageExportInfo = vm.getImageExportInfo();
        if (imageExportInfo != null && StringUtils.isNotEmpty(imageExportInfo.getImageInstanceId())) {
            HttpClientUtil.deleteVmImage(basePath, user.getUserId(), vmInstantiateInfo.getAppInstanceId(),
                imageExportInfo.getImageInstanceId(), user.getToken());
        }
        if (vmInstantiateInfo == null) {
            return;
        }
        if (StringUtils.isNotEmpty(vmInstantiateInfo.getMepmPackageId()) || StringUtils
            .isNotEmpty(vmInstantiateInfo.getAppInstanceId())) {
            sentTerminateRequestToLcm(basePath, user.getUserId(), user.getToken(), vmInstantiateInfo.getAppInstanceId(),
                vmInstantiateInfo.getMepmPackageId(), mepHost.getMecHostIp());
        }
        boolean deleteRes = appPackageService.deletePackage(vmInstantiateInfo.getAppPackageId());
        if (!deleteRes) {
            LOGGER.info("delete InstantiateInfo fail, vmId:{}", vm.getId());
        }
        deleteExportInfo(vm.getId());
        deleteInstantiateInfo(vm.getId());
    }

    private Boolean pushFileToVm(File appFile, String applicationId, String vmId) {
        Application application = applicationService.getApplication(applicationId);
        String hostId = application.getMepHostId();
        MepHost mepHost = mepHostService.getHost(hostId);
        VirtualMachine vm = vmAppVmServiceImpl.getVm(applicationId, vmId);
        VMInstantiateInfo vmInstantiateInfo = vm.getVmInstantiateInfo();
        String username = vm.getVmCertificate().getPwdCertificate().getUsername();
        String password = vm.getVmCertificate().getPwdCertificate().getPassword();
        List<PortInstantiateInfo> portInstantiateInfos = vmInstantiateInfo.getPortInstanceList();
        PkgSpec pkgSpec = pkgSpecService.getPkgSpecById(application.getPkgSpecId());
        String defaultNetworkName =
            AppdConstants.NETWORK_NAME_PREFIX + pkgSpec.getSpecifications().getAppdSpecs().getNetworkNameSpecs()
                .getNetworkNameN6();
        Map<String, String> vmInputParams = InputParameterUtil.getParams(mepHost.getNetworkParameter());
        String networkName = vmInputParams.getOrDefault("APP_Plane03_Network", defaultNetworkName);
        LOGGER.info("defaultNetworkName:{}, networkName:{}", defaultNetworkName, networkName);
        String networkIp = "";
        for (PortInstantiateInfo portInstantiateInfo : portInstantiateInfos) {
            if (portInstantiateInfo.getNetworkName().equals(networkName)) {
                networkIp = portInstantiateInfo.getIpAddress();
            }
        }
        LOGGER.info("network Ip, username is {},{}", networkIp, username);
        // ssh upload file
        String targetPath = "";
        ScpConnectEntity scpConnectEntity = new ScpConnectEntity();
        scpConnectEntity.setTargetPath(targetPath);
        scpConnectEntity.setUrl(networkIp);
        scpConnectEntity.setPassWord(password);
        scpConnectEntity.setUserName(username);
        String remoteFileName = appFile.getName();
        LOGGER.info("path:{}，filename:{}", targetPath, remoteFileName);
        ShhFileUploadUtil sshFileUploadUtil = new ShhFileUploadUtil();
        FileUploadEntity fileUploadEntity = sshFileUploadUtil.uploadFile(appFile, remoteFileName, scpConnectEntity);
        if (fileUploadEntity.getCode().equals("ok")) {
            return true;
        } else {
            LOGGER.warn("upload fail, ip:{}", networkIp);
            return false;
        }

    }

    private String getUploadFileRootDir() {
        return tempUploadPath + File.separator + SUBDIR_FILE + File.separator;
    }

}
