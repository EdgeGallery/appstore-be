package org.edgegallery.appstore.interfaces.project;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkLoadStatus {
    private List<ServiceFromLcm> services = new ArrayList<>();
}
