package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.interfaces.meao.facade.ProgressFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UploadHelper {
    public static final Logger LOGGER = LoggerFactory.getLogger(UploadHelper.class);

    @Autowired
    ProgressFacade progressFacade;

    /**
     * upload Big Software.
     *
     * @param softPath package path
     * @param req request body
     * @param csrfToken token
     * @param cookie cookie
     * @param hostUrl request url
     * @return JSONObject
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
            input = new FileInputStream(soft);

            UploadPackageEntity upPackage = new UploadPackageEntity();
            upPackage.setFileName(fileName);
            upPackage.setFileIdentify(System.currentTimeMillis());
            upPackage.setCookie(cookie);
            upPackage.setCsrfToken(csrfToken);
            long length = soft.length();
            long totalSize = length;
            upPackage.setTotalSie(totalSize);

            // build upload progress data
            String progressId = UUID.randomUUID().toString();
            Date createTime = Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            PackageUploadProgress progress = new PackageUploadProgress(progressId, req.getString("packageId"),
                req.getString("meaoId"), createTime);
            progressFacade.createProgress(progress);
            int shardTotal = (int) (totalSize / AppConfig.FILE_SIZE);

            //shard size 9437980
            byte[] buffer = new byte[AppConfig.FILE_SIZE];
            while (length > AppConfig.FILE_SIZE && input.read(buffer, 0, AppConfig.FILE_SIZE) != -1) {
                header.put("Content-Length", AppConfig.FILE_SIZE);
                j = i + AppConfig.FILE_SIZE;
                header.put("X-File-start", i);
                header.put("X-File-end", j);
                String url = AppConfig.UPLOAD_PATH.replace("${taskName}", req.getString("taskName")) + count;
                upPackage.setShardCount(count);
                ret = Connection.postFiles(header, "https://" + hostUrl + url, upPackage, req, buffer);
                if (ret.getInteger("retCode") == -1) {
                    return ret;
                }
                LOGGER.info("upload file：" + fileName + "-total size：" + totalSize + "-already upload：" + i);

                // update upload progress
                if (count % 10 == 0) {
                    progress.setProgress(String.valueOf(count * 100 / totalSize));
                    progressFacade.updateProgress(progress);
                }

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
            upPackage.setShardCount(count);
            ret = Connection.postFiles(header, "https://" + hostUrl + url, upPackage, req, ednBuffer);
            LOGGER.info("upload file：" + fileName + "-total size：" + totalSize + "-already upload：" + soft.length());
            LOGGER.info(fileName + " Upload package finished.");

            // update upload progress to 100%
            progress.setProgress("100");
            progressFacade.updateProgress(progress);

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
