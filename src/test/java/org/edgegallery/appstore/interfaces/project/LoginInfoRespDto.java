package org.edgegallery.appstore.interfaces.project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class LoginInfoRespDto {
    private Object userId;

    private Object userName;

    private Object isSecureBackend;

    private Object loginPage;

    private Object userCenterPage;

    private Object forceModifyPwPage;

    private Object accessToken;

    private Object authorities;

    private Object sessId;
}
