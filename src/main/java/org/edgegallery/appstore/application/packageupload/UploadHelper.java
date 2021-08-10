package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadHelper {
    public static final Logger LOGGER = LoggerFactory.getLogger(UploadHelper.class);

    /**
     * upload Big Software
     *
     * @param softPath
     * @param req
     * @param csrfToken
     * @param cookie
     * @param hostUrl
     * @return
     */
    public JSONObject uploadBigSoftware(String softPath, JSONObject req, String csrfToken, String cookie,
        String hostUrl) {
        JSONObject ret = new JSONObject();
        FileInputStream input = null;
        try {
            File soft = new File(softPath);
            String fileName = soft.getName();

            LOGGER.info("--------start upload software--------" + fileName);
            JSONObject header = new JSONObject();
            UUID uuid = UUID.randomUUID();
            String fileId = uuid.toString().replace("-", "");
            header.put("X-File-id", fileId);
            header.put("X-File-Name", fileName);
            header.put("X-File-size", soft.length());
            header.put("X-Uni-Crsf-Token", csrfToken);
            header.put("X_Requested_With", "XMLHttpRequest");
            header.put("Content-Type", "application/octet-stream");
            header.put("Cache-Control", "no-cache");
            header.put("Host", hostUrl.split(":")[0] + ":" + hostUrl.split(":")[1]);

            long i = 0;
            long j;
            int count = 1;
            long length = soft.length();
            long totalSize = length;
            input = new FileInputStream(soft);

            UploadPackageEntity upPackage = new UploadPackageEntity();
            upPackage.setFileName(fileName);
            upPackage.setCookie(cookie);
            upPackage.setCsrfToken(csrfToken);
            upPackage.setTotalSie(totalSize);

            //分片大小9437980
            byte[] buffer = new byte[AppConfig.FILE_SIZE];
            while (length > AppConfig.FILE_SIZE && input.read(buffer, 0, AppConfig.FILE_SIZE) != -1) {
                header.put("Content-Length", AppConfig.FILE_SIZE);
                j = i + AppConfig.FILE_SIZE;
                header.put("X-File-start", i);
                header.put("X-File-end", j);
                String url = AppConfig.UPLOAD_PATH.replace("${taskName}", req.getString("taskName")) + count;
                upPackage.setPostData(buffer);
                upPackage.setShardCount(count);
                ret = Connection.postFiles(header, hostUrl + url, upPackage, req);
                LOGGER.info("上传文件：" + fileName + "-总大小：" + totalSize + "-已上传：" + i);
                i = j;
                count++;
                length = length - AppConfig.FILE_SIZE;
            }

            byte[] ednBuffer = new byte[(int) length];
            int readCount = input.read(ednBuffer);
            if (readCount == -1) {
                return ret;
            }
            header.put("Content-Length", length);
            header.put("X-File-start", i);
            header.put("X-File-end", soft.length());
            String url = AppConfig.UPLOAD_PATH.replace("${taskName}", req.getString("taskName")) + count;
            upPackage.setPostData(ednBuffer);
            upPackage.setShardCount(count);
            ret = Connection.postFiles(header, hostUrl + url, upPackage, req);
            LOGGER.info("上传文件：" + fileName + "-总大小：" + totalSize + "-已上传：" + soft.length());
            LOGGER.info("Upload package finished.");
            return ret;
        } catch (IOException e) {
            LOGGER.error("uploadBigSoftware IOException");
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                LOGGER.error("uploadBigSoftware close input IOException");
            }
        }
        ret.put("retCode", -1);
        return ret;
    }
}
