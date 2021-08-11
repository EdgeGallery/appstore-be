package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/package")
public class UploadPackageController {
    @Autowired
    private UploadService uploadService;

    @RequestMapping(value = "/v1/upload/appd", method = RequestMethod.POST)
    public JSONObject uploadPackage(@RequestBody String req) {
        return uploadService.uploadPackage(req);
    }
}
