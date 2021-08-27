package org.edgegallery.appstore.interfaces.app.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppCtrlDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppReqDto;

public class TestMain {
    public static void main(String[] args) {
        QueryAppReqDto queryAppReqDto = new QueryAppReqDto();
        queryAppReqDto.setAffinity(new ArrayList());
        queryAppReqDto.setIndustry(new ArrayList());
        queryAppReqDto.setTypes(new ArrayList());
        queryAppReqDto.setWorkloadType(new ArrayList());
        queryAppReqDto.setQueryCtrl(new QueryAppCtrlDto());

        Gson g = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        // System.out.println(g.toJson(queryAppReqDto));

        List<AppDto> list = new ArrayList<>();
        list.add(new AppDto());
        Page<AppDto> page = new Page<AppDto>(list, 0,0,0);
        System.out.println(g.toJson(page));
    }
}
