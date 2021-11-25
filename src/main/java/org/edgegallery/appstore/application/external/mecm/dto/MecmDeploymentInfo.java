package org.edgegallery.appstore.application.external.mecm.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Component("mecmDeploymentInfo")
public class MecmDeploymentInfo {
    private String mecmAppInstanceId;
    private String mecmOperationalStatus;
    private String mecmAppId;
    private String mecmAppPackageId;


}
