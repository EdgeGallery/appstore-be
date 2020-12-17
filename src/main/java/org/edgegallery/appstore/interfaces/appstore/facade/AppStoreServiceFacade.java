package org.edgegallery.appstore.interfaces.appstore.facade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("AppStoreServiceFacade")
public class AppStoreServiceFacade {
    /**
     * add app store.
     */
    public ResponseEntity<AppStoreDto> addAppStore(AppStoreDto appStoreDto) {
        System.out.println("name : " + appStoreDto.getAppStoreName());
        System.out.println("version : " + appStoreDto.getAppStoreVersion());
        System.out.println("company : " + appStoreDto.getCompany());
        System.out.println("url : " + appStoreDto.getUrl());
        System.out.println("schema : " + appStoreDto.getSchema());
        System.out.println("appPushIntf : " + appStoreDto.getAppPushIntf());
        System.out.println("appdTransId : " + appStoreDto.getAppdTransId());
        System.out.println("description : " + appStoreDto.getDescription());
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = dateFormat.format(date);
        appStoreDto.setAddedTime(current);
        appStoreDto.setModifiedTime(current);
        appStoreDto.setAppStoreId("12345678-1234-1234-1234-123456789012");
        return ResponseEntity.ok(appStoreDto);
    }

    /**
     * delete app store.
     */
    public ResponseEntity<String> deleteAppStore(String appStoreId) {
        System.out.println("appStoreId : " + appStoreId);
        return null;
    }

    /**
     * edit app store.
     */
    public ResponseEntity<AppStoreDto> editAppStore(AppStoreDto appStoreDto) {
        System.out.println("appStoreId : " + appStoreDto.getAppStoreId());
        System.out.println("name : " + appStoreDto.getAppStoreName());
        System.out.println("version : " + appStoreDto.getAppStoreVersion());
        System.out.println("company : " + appStoreDto.getCompany());
        System.out.println("url : " + appStoreDto.getUrl());
        System.out.println("schema : " + appStoreDto.getSchema());
        System.out.println("appPushIntf : " + appStoreDto.getAppPushIntf());
        System.out.println("appdTransId : " + appStoreDto.getAppdTransId());
        System.out.println("description : " + appStoreDto.getDescription());
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = dateFormat.format(date);
        appStoreDto.setAddedTime(current);
        appStoreDto.setModifiedTime(current);
        return ResponseEntity.ok(appStoreDto);
    }

    /**
     * query app stores.
     */
    public ResponseEntity<List<AppStoreDto>> queryAppStores(String name, String company) {
        System.out.println("name : " + name);
        System.out.println("company : " + company);
        List<AppStoreDto> list = new ArrayList<>();
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = dateFormat.format(date);
        AppStoreDto appStoreDto = new AppStoreDto("12345678-1234-1234-1234-123456789012",
                "AppStore1", "V1.1.0", "Unicom", "/a/b/c/d", "http",
                "/qq/ww/ee", "12345678-1234-1234-1234-123456789012",
                "description", current, current);
        list.add(appStoreDto);
        appStoreDto = new AppStoreDto("abcdefgh-1234-1234-1234-12345678abcd",
                "AppStore2", "V1.1.0", "Telecom", "/a/b/c/d", "http",
                "/qq/ww/ee", "12345678-1234-1234-1234-123456789012",
                "description", current, current);
        list.add(appStoreDto);
        return ResponseEntity.ok(list);
    }

    /**
     * query app store.
     */
    public ResponseEntity<AppStoreDto> queryAppStore(String appStoreId) {
        System.out.println("appStoreId : " + appStoreId);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = dateFormat.format(date);
        AppStoreDto appStoreDto = new AppStoreDto("12345678-1234-1234-1234-123456789012",
                "AppStore1", "V1.1.0", "Unicom", "/a/b/c/d", "http",
                "/qq/ww/ee", "12345678-1234-1234-1234-123456789012",
                "description", current, current);
        return ResponseEntity.ok(appStoreDto);
    }
}
