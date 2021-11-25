package org.edgegallery.appstore.application.external.mecm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Component("mecmInfo")
public class MecmInfo {
    private String mecmAppId;
    private String mecmAppPackageId;
}
