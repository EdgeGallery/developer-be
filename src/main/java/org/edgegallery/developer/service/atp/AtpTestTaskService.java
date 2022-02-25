package org.edgegallery.developer.service.atp;

import java.util.List;
import org.edgegallery.developer.model.atp.AtpTest;

public interface AtpTestTaskService {

    /**
     * create atp test.
     *
     * @param applicationId applicationId
     * @param atpTest encapsulated atp class
     * @return
     */
    boolean createAtpTest(String applicationId, AtpTest atpTest);

    /**
     * get all atp tests.
     *
     * @param applicationId applicationId
     * @return
     */
    List<AtpTest> getAtpTests(String applicationId);

    /**
     * get atp test by id.
     *
     * @param id atp test id
     * @return
     */
    AtpTest getAtpTestById(String id);

    /**
     * update atp test status.
     *
     * @param task needed update atp test
     * @return
     */
    boolean updateAtpTestStatus(AtpTest task);

}
