package org.edgegallery.appstore.interfaces.apackage.facade.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushTargetAppStoreDto {
    private List<String> targetPlatform;
}
