package org.edgegallery.appstore.application.external;

import org.edgegallery.appstore.application.external.model.AtpTestDto;
import org.edgegallery.appstore.domain.model.releases.Release;

public interface AtpServiceInterface {

    String getAtpTaskResult(String token, String taskId);

    AtpTestDto createTestTask(Release release, String token);
}
