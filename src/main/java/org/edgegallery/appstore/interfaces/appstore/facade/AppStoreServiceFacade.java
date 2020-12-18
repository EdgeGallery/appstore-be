package org.edgegallery.appstore.interfaces.appstore.facade;

import java.util.List;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.appstore.AppStore;
import org.edgegallery.appstore.domain.model.appstore.AppStoreRepository;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("AppStoreServiceFacade")
public class AppStoreServiceFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStoreServiceFacade.class);

    @Autowired
    private AppStoreRepository appStoreRepository;

    /**
     * add app store.
     */
    public ResponseEntity<AppStoreDto> addAppStore(AppStoreDto appStoreDto) {
        /*
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
        */
        String uuid = appStoreRepository.addAppStore(AppStore.of(appStoreDto));
        AppStore appStore = appStoreRepository.queryAppStoreById(uuid);
        if (appStore == null) {
            LOGGER.error("failed to add app store : " + appStoreDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(appStoreDto);
        }
        return ResponseEntity.ok(appStore.toAppStoreDto());
    }

    /**
     * delete app store.
     */
    public ResponseEntity<String> deleteAppStore(String appStoreId) {
        // System.out.println("appStoreId : " + appStoreId);
        appStoreRepository.deleteAppStoreById(appStoreId);
        return ResponseEntity.ok("");
    }

    /**
     * edit app store.
     */
    public ResponseEntity<AppStoreDto> editAppStore(AppStoreDto appStoreDto) {
        /*
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
        */
        if (appStoreRepository.updateAppStoreById(AppStore.of(appStoreDto)) != 1) {
            LOGGER.error("failed to edit app store : " + appStoreDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(appStoreDto);
        }
        AppStore appStore = appStoreRepository.queryAppStoreById(appStoreDto.getAppStoreId());
        return ResponseEntity.ok(appStore.toAppStoreDto());
    }

    /**
     * query app stores.
     */
    public ResponseEntity<List<AppStoreDto>> queryAppStores(String name, String company) {
        /*
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
        */
        AppStore appStore = new AppStore(name, company);
        List<AppStore> appStoreList = appStoreRepository.queryAppStores(appStore);
        List<AppStoreDto> dtoList = appStoreList.stream().map(item -> item.toAppStoreDto())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * query app store.
     */
    public ResponseEntity<AppStoreDto> queryAppStore(String appStoreId) {
        /*
        System.out.println("appStoreId : " + appStoreId);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current = dateFormat.format(date);
        AppStoreDto appStoreDto = new AppStoreDto("12345678-1234-1234-1234-123456789012",
                "AppStore1", "V1.1.0", "Unicom", "/a/b/c/d", "http",
                "/qq/ww/ee", "12345678-1234-1234-1234-123456789012",
                "description", current, current);
        */
        AppStore appStore = appStoreRepository.queryAppStoreById(appStoreId);
        return ResponseEntity.ok(appStore == null ? new AppStoreDto() : appStore.toAppStoreDto());
    }
}
