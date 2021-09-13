package org.edgegallery.developer.service.uploadfile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;

@Service("uploadService")
public class UploadServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private static Gson gson = new Gson();

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    /**
     * save Config File.
     *
     * @param uploadFile config file
     * @param userId userid
     * @return
     */
    public UploadedFile saveFileToLocal(MultipartFile uploadFile, String userId) {
        UploadedFile result = new UploadedFile();
        String fileName = uploadFile.getOriginalFilename();
        String fileId = UUID.randomUUID().toString();
        String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
        String fileRealPath = upLoadDir + fileId;
        File dir = new File(upLoadDir);

        if (!dir.isDirectory()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                LOGGER.error("make file dir failed");
                return null;
            }
        }
        File newFile = new File(fileRealPath);
        try {
            uploadFile.transferTo(newFile);
            result.setFileName(fileName);
            result.setFileId(fileId);
            result.setUserId(userId);
            result.setUploadDate(new Date());
            result.setTemp(true);
            result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
            uploadedFileMapper.saveFile(result);
        } catch (IOException e) {
            LOGGER.error("Failed to save file.");
            return null;
        }
        LOGGER.info("upload file success {}", fileName);
        //upload success
        result.setFilePath("");
        return result;
    }

    /**
     * moveFileToWorkSpaceById.
     */
    public void moveFileToWorkSpaceById(String srcId, String applicationId) throws IOException {
        uploadedFileMapper.updateFileStatus(srcId, false);
        // to confirm, whether the status is updated
        UploadedFile file = uploadedFileMapper.getFileById(srcId);
        if (file == null || file.isTemp()) {
            uploadedFileMapper.updateFileStatus(srcId, true);
            LOGGER.error("Can not find file, please upload again.");
            throw new DeveloperException("Can not find file", ResponseConsts.QUERY_DATA_FAILED);
        }
        // get temp file
        String tempFilePath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + srcId;
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists() || tempFile.isDirectory()) {
            uploadedFileMapper.updateFileStatus(srcId, true);
            LOGGER.error("Can not find file, please upload again.");
            throw new DeveloperException("Can not find file", ResponseConsts.QUERY_DATA_FAILED);
        }
        // move file
        File desFile = new File(DeveloperFileUtils.getAbsolutePath(applicationId) + srcId);
        try {
            DeveloperFileUtils.moveFile(tempFile, desFile);
            String filePath = BusinessConfigUtil.getWorkspacePath() + applicationId + File.separator + srcId;
            uploadedFileMapper.updateFilePath(srcId, filePath);
        } catch (IOException e) {
            LOGGER.error("move icon file failed {}", e.getMessage());
            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new DeveloperException("Move icon file failed.", ResponseConsts.QUERY_DATA_FAILED);
        }
    }
}
