package org.edgegallery.developer.service.atp;

import java.util.List;
import org.edgegallery.developer.model.atp.AtpTest;

public interface AtpTestTaskService {

    boolean createAtpTest(String applicationId, AtpTest atpTest);

    List<AtpTest> getAtpTests(String applicationId);

    AtpTest getAtpTestById(String id);

    boolean updateAtpTestStatus(AtpTest task);

    boolean deleteAtpTestByAppId(String applicationId);
}
