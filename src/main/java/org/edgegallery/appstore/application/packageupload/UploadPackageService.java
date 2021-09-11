package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("UploadPackageService")
public class UploadPackageService {
    @Autowired
    ThirdSystemMapper thirdSystemMapper;

    @Autowired
    UploadHelper uploadHelper;

    /**
     * uploadPackage.
     *
     * @param filePath file path
     * @return JSONObject
     */
    public JSONObject uploadPackage(String filePath, String packageId, String meaoId) {
        // mock data
        filePath = "D:\\OSDT\\APPD\\edgegallery_vm_meo_attributes\\edgegallery_vm_meo_attributes1.zip";
        packageId = "12345";
        meaoId = "e87650e5-3f0f-4688-8302-736acb1dac31";

        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String taskName = fileName.substring(0, fileName.indexOf("."));
        JSONObject reqJson = new JSONObject();
        reqJson.put("taskName", taskName);
        reqJson.put("packageId", packageId);
        reqJson.put("meaoId", meaoId);

        ServiceDef serviceDef = new ServiceDef();
        serviceDef.setName(taskName);
        serviceDef.setServiceType("vnfpackage");
        serviceDef.setSpecification("APP");
        serviceDef.setAction("create");
        serviceDef.setMode("normal");
        serviceDef.setFileName(fileName);
        JSONObject vnfpackageInfo = new JSONObject();
        vnfpackageInfo.put("serviceDef", serviceDef);
        reqJson.put("vnfpackageInfo", vnfpackageInfo);

        // query meao info by id
        ThirdSystem meaoInfo = thirdSystemMapper.selectByPrimaryKey(meaoId);
        if (meaoInfo == null) {
            throw new AppException("get meao info fail.");
        }

        String meaoUrl = meaoInfo.getUrl();
        JSONObject session = Utils.getSessionCookie(meaoUrl, meaoInfo.getUsername(), meaoInfo.getPassword());
        JSONObject cookieInfo = JSON.parseObject(session.getString("body"));
        String csrfToken = cookieInfo.getString("csrfToken");
        String cookie = cookieInfo.getString("session");

        String meaoHost = meaoUrl.split("//")[1];
        return uploadHelper.uploadBigSoftware(filePath, reqJson, csrfToken, cookie, meaoHost);
    }
}
