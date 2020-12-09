package org.edgegallery.appstore.application.external.atp;

import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.model.releases.Release;

public interface AtpServiceInterface {

    String getAtpTaskResult(String token, String taskId);

    AtpTestDto createTestTask(Release release, String token);
}
