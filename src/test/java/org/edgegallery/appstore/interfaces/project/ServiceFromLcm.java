package org.edgegallery.appstore.interfaces.project;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceFromLcm {
    private String serviceName;
    private List<PortFromLcm> ports = new ArrayList<>();
}
