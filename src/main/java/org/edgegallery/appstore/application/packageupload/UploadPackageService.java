package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("UploadPackageService")
public class UploadPackageService {
    @Value("${meao.meao-host}")
    private String meaoHost;

    @Value("${meao.meao-user}")
    private String meaoUser;

    @Value("${meao.meao-password}")
    private String meaoPassword;

    /**
     * uploadPackage.
     *
     * @param filePath file path
     * @return JSONObject
     */
    public JSONObject uploadPackage(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String taskName = fileName.substring(0, fileName.indexOf("."));
        JSONObject reqJson = new JSONObject();
        reqJson.put("taskName", taskName);

        JSONObject vnfpackageInfo = new JSONObject();
        ServiceDef serviceDef = new ServiceDef();
        serviceDef.setName(taskName);
        serviceDef.setServiceType("vnfpackage");
        serviceDef.setSpecification("APP");
        serviceDef.setAction("create");
        serviceDef.setMode("normal");
        serviceDef.setFileName(fileName);
        vnfpackageInfo.put("serviceDef", serviceDef);
        reqJson.put("vnfpackageInfo", vnfpackageInfo);

        JSONObject session = Utils.getSessionCookie(meaoHost, meaoUser, meaoPassword);
        JSONObject cookieInfo = JSON.parseObject(session.getString("body"));
        String csrfToken = cookieInfo.getString("csrfToken");
        String cookie = cookieInfo.getString("session");

        UploadHelper helper = new UploadHelper();
        return helper.uploadBigSoftware(filePath, reqJson, csrfToken, cookie, meaoHost);
    }
}
