package org.edgegallery.appstore.interfaces.project;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeResponse;

@Getter
@Setter
public class DistributeResponseDto {
    private List<DistributeResponse> data;
}
