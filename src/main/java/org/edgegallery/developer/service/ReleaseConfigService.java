package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ReleaseConfigMapper;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("releaseConfigService")
public class ReleaseConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseConfigService.class);

    @Autowired
    private ReleaseConfigMapper configMapper;

    public Either<FormatRespDto, ReleaseConfig> saveConfig(String projectId, ReleaseConfig config) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        String releaseId = UUID.randomUUID().toString();
        config.setReleaseId(releaseId);
        config.setProjectId(projectId);
        config.setCreateTime(new Date());
        int res = configMapper.saveConfig(config);
        if (res < 1) {
            LOGGER.error("save config data fail!");
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
            return Either.left(dto);
        }
        LOGGER.info("create release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));
    }

    public Either<FormatRespDto, ReleaseConfig> modifyConfig(String projectId, ReleaseConfig config) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ReleaseConfig oldConfig = configMapper.getConfigByProjectId(projectId);
        if (oldConfig == null) {
            LOGGER.error("projectId error,can not find any project!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId error!");
            return Either.left(dto);
        }

        config.setReleaseId(oldConfig.getReleaseId());
        config.setProjectId(projectId);
        config.setCreateTime(new Date());

        int res = configMapper.modifyReleaseConfig(config);
        if (res < 1) {
            LOGGER.error("create project {} release-config data fail!", projectId);
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
            return Either.left(dto);
        }
        LOGGER.info("modify release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));

    }

    public Either<FormatRespDto, ReleaseConfig> getConfigById(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ReleaseConfig oldConfig = configMapper.getConfigByProjectId(projectId);
        if (oldConfig == null) {
            LOGGER.error("projectId error,can not find any project!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId error!");
            return Either.left(dto);
        }
        LOGGER.info("get release config success!");
        return Either.right(oldConfig);

    }
}
