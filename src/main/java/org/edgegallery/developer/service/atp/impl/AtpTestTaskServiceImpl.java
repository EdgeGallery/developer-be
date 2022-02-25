package org.edgegallery.developer.service.atp.impl;

import java.util.List;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.mapper.atp.AtpTestTaskMapper;
import org.edgegallery.developer.model.atp.AtpTest;
import org.edgegallery.developer.service.atp.AtpTestTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("atpTestTaskService")
public class AtpTestTaskServiceImpl implements AtpTestTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtpTestTaskServiceImpl.class);

    @Autowired
    private AtpTestTaskMapper atpTestTaskMapper;

    @Override
    public boolean createAtpTest(String applicationId, AtpTest atpTest) {
        int res = atpTestTaskMapper.createAtpTest(applicationId, atpTest);
        if (res < 1) {
            LOGGER.error("create atp test task failed!");
            throw new DataBaseException("create atp test task failed!", ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public List<AtpTest> getAtpTests(String applicationId) {
        return atpTestTaskMapper.getAtpTests(applicationId);
    }

    @Override
    public AtpTest getAtpTestById(String id) {
        return atpTestTaskMapper.getAtpTestById(id);
    }

    @Override
    public boolean updateAtpTestStatus(AtpTest task) {
        int res = atpTestTaskMapper.updateAtpTestStatus(task);
        if (res < 1) {
            LOGGER.error("update atp test status failed!");
            throw new DataBaseException("update atp test status failed!", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public void deleteAtpTestByAppId(String applicationId) {
        int res = atpTestTaskMapper.deleteAtpTestByAppId(applicationId);
        if (res < 1) {
            LOGGER.warn("delete atp test failed with applicationId:{}", applicationId);
        }
    }
}
