package org.edgegallery.developer.mapper.uploadfile;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.developer.model.workspace.UploadedFile;

@Mapper
public interface UploadMapper {
    UploadedFile getFileById(String fileId);

    int saveFile(UploadedFile file);

    int updateFile(UploadedFile file);

    int updateFileStatus(String fileId, boolean isTemp);

    int updateFilePath(String fileId, String filePath);

    int deleteFile(String fileId);

    List<String> getAllTempFiles();
}
