package org.edgegallery.appstore.domain.model.appd;

import java.util.Map;

public interface IParamsHandler {

    void setData(Map.Entry<String, String> data);

    boolean checkParams();

    String toString();

    Map.Entry<String, String> getFirstData();

}
