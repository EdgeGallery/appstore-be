package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service("UploadService")
public class UploadService {
    public JSONObject uploadPackage(String reqStr) {
        JSONObject reqJson = JSONObject.parseObject(reqStr);
        String hostUrl = reqJson.getString("hostIp");
        String userName = reqJson.getString("userName");
        String password = reqJson.getString("passWord");
        String filePath = reqJson.getString("filePath");

        JSONObject session = Utils.getSessionCookie(hostUrl, userName, password);
        JSONObject cookieInfo = JSON.parseObject(session.getString("body"));
        String csrfToken = cookieInfo.getString("csrfToken");
        String cookie = cookieInfo.getString("session");

        UploadHelper helper = new UploadHelper();
        return helper.uploadBigSoftware(filePath, reqJson, csrfToken, cookie, hostUrl);
    }
}
