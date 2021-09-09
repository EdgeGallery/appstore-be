package org.edgegallery.appstore.interfaces.meao.facade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ThirdSystemDto {
    private String id;

    private String systemName;

    private String systemType;

    private String ip;

    private String port;

    private String username;

    private String password;

    private String vendor;
}
