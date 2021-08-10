package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSONObject;
import java.util.concurrent.Callable;

public class UploadThread implements Callable<JSONObject> {
    private JSONObject header;

    private String url;

    private byte[] postData;

    private long totalSize;

    private int count;

    private String fileName;

    private JSONObject req;

    private String csrfToken;

    private String cookie;

    public UploadThread(JSONObject header, String url, byte[] postData, long totalSize, int count, String fileName,
        JSONObject req, String csrfToken, String cookie) {
        this.header = header;
        this.url = url;
        this.postData = postData;
        this.totalSize = totalSize;
        this.count = count;
        this.fileName = fileName;
        this.req = req;
        this.csrfToken = csrfToken;
        this.cookie = cookie;
    }

    @Override
    public JSONObject call() throws Exception {
        JSONObject ret = Connection
            .postFiles(header, AppConfig.UPLOAD_PATH.replace("${taskName}", req.getString("taskName")) + count,
                postData, totalSize, count, fileName, req, csrfToken, cookie);
        return ret;
    }
}
