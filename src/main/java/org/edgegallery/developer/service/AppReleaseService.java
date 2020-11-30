package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.model.AppPkgStructure;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("appReleaseService")
public class AppReleaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppReleaseService.class);

    private List<String> listLocal = new ArrayList<>();

    /**
     * getPkgStruById.
     */
    public Either<FormatRespDto, AppPkgStructure> getPkgStruById(String projectId, String csarId) {
        if (projectId == null || projectId.equals("")) {
            LOGGER.error("project id can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!");
            return Either.left(error);
        }
        String csarPath = getProjectPath(projectId);
        if (csarPath == null || csarPath.equals("")) {
            LOGGER.error("can not find this project!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find this project!");
            return Either.left(error);
        }
        File csarFile = new File(csarPath + csarId + ".csar");
        //unzip csar file
        boolean isSuccess = unZipFiles(csarFile, getProjectPath(projectId));
        if (!isSuccess) {
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "unzip csar file fail!");
            return Either.left(error);
        }
        // get csar pkg structure
        AppPkgStructure structure;
        try {
            structure = getFiles(getProjectPath(projectId) + csarId + File.separator, new AppPkgStructure());
        } catch (IOException ex) {
            LOGGER.error("get csar pkg occur io exception: {}", ex.getMessage());
            String message = "get csar pkg occur io exception!";
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, message);
            return Either.left(error);
        }
        return Either.right(structure);
    }

    /**
     * getPkgContentByFileName.
     */
    public Either<FormatRespDto, String> getPkgContentByFileName(String projectId, String fileName) {
        if (projectId == null || projectId.equals("")) {
            LOGGER.error("project id can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!");
            return Either.left(error);
        }
        if (fileName == null || fileName.equals("")) {
            LOGGER.error("project id can not be empty!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file name can not be empty!");
            return Either.left(error);
        }
        File file = new File(getProjectPath(projectId));
        List<String> paths = getFilesPath(file);
        if (paths == null || paths.size() == 0) {
            LOGGER.error("can not find any file!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any file!");
            return Either.left(error);
        }
        String fileContent = "";
        for (String path : paths) {
            if (path.contains(fileName)) {
                fileContent = readFileIntoString(path);
            }
        }
        if (fileContent.equals("")) {
            LOGGER.warn("file has not any content!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is null!");
            return Either.left(error);
        }

        if (fileContent.equals("error")) {
            LOGGER.warn("file is not readable!");
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "file is not readable!");
            return Either.left(error);
        }
        return Either.right(fileContent);
    }

    private boolean unZipFiles(File zipFile, String descDir) {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            boolean isMk = pathFile.mkdirs();
            isSuccess(isMk);
        }
        // ZipFile zip = null;
        try (ZipFile zip = new ZipFile(zipFile);) {
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                try (InputStream in = zip.getInputStream(entry)) {
                    String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
                    //判断路径是否存在,不存在则创建文件路径
                    File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                    if (!file.exists()) {
                        boolean isMk = file.mkdirs();
                        isSuccess(isMk);
                    }
                    //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                    if (new File(outPath).isDirectory()) {
                        continue;
                    }
                    //输出文件路径信息
                    try (OutputStream out = new FileOutputStream(outPath)) {
                        byte[] buf1 = new byte[1024];
                        int len;
                        try {
                            while ((len = in.read(buf1)) > 0) {
                                out.write(buf1, 0, len);
                            }
                        } catch (IOException e) {
                            LOGGER.error("unzip pkg occur io exception {}", e.getMessage());
                            return false;
                        }
                    } catch (FileNotFoundException e) {
                        LOGGER.error("unzip pkg occur file not found exception {}", e.getMessage());
                        return false;
                    }
                } catch (IOException e) {
                    LOGGER.error("unzip pkg occur io exception {}", e.getMessage());
                    return false;
                }
            }
        } catch (IOException e) {
            LOGGER.error("unzip pkg occur zip exception {}", e.getMessage());
            return false;
        }
        return true;
    }

    private void isSuccess(boolean bool) {
        if (!bool) {
            LOGGER.error("create folder fail");
        }
    }

    private String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
            + File.separator;
    }

    private AppPkgStructure getFiles(String filePath, AppPkgStructure appPkgStructure) throws IOException {
        File root = new File(filePath);
        File[] files = root.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<AppPkgStructure> fileList = new ArrayList<>();
        for (File file : files) {
            AppPkgStructure dto = new AppPkgStructure();
            if (file.isDirectory()) {
                String str = file.getName();
                dto.setId(str);
                dto.setName(str);
                fileList.add(dto);
                //递归调用
                File[] fileArr = file.listFiles();
                if (fileArr != null && fileArr.length != 0) {
                    getFiles(file.getCanonicalPath(), dto);
                }
            } else {
                AppPkgStructure valueDto = new AppPkgStructure();
                valueDto.setId(file.getName());
                valueDto.setName(file.getName());
                valueDto.setParent(false);
                fileList.add(valueDto);
            }
        }
        appPkgStructure.setChildren(fileList);
        return appPkgStructure;
    }

    private List<String> getFilesPath(File dir) {
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null && files.length != 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getFilesPath(file);
                    }
                    if (file.isFile()) {
                        try {
                            listLocal.add(file.getCanonicalPath());
                        } catch (IOException e) {
                            LOGGER.error("get unzip dir occur exception {}", e.getMessage());
                            return new ArrayList<>();
                        }
                    }
                }
            }
        }
        return listLocal;
    }

    private String readFileIntoString(String filePath) {
        String msg = "error";
        StringBuffer sb = new StringBuffer();
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    sb.append(lineTxt + "\r\n");
                }
                bufferedReader.close();
                read.close();
            } else {
                LOGGER.error("There are no files in this directory!");
                return msg;
            }
        } catch (Exception e) {
            LOGGER.error("read file occur exception {}", e.getMessage());
            return msg;
        }

        return sb.toString();
    }
}
