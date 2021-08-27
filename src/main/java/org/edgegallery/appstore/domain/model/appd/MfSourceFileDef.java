package org.edgegallery.appstore.domain.model.appd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class MfSourceFileDef implements IParamsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSourceFileDef.class);

    private final Map<String, String> params = new LinkedHashMap<>();
    public final static String startFlag = ParamType.values()[0].name;

    public void setData(Map.Entry<String, String> data) {
        if (ParamType.of(data.getKey()) != null) {
            params.put(data.getKey(), data.getValue());
        }
    }

    public boolean checkParams() {
        for (ParamType type : ParamType.values()) {
            if (type.isNotNull && !params.containsKey(type.name)) {
                LOGGER.info("not include param {} in the MF file.", type.name);
                return false;
            }
        }
        return true;
    }

    public String toString() {
        List<String> lines = new ArrayList<>();
        params.forEach((key, value) -> lines.add(String.format("%s: %s", key, value)));
        return StringUtils.join(lines, "\n");
    }

    private enum ParamType {
        Source("Source", true),
        Algorithm("Algorithm", true),
        Hash("Hash", true);

        private String name;

        private boolean isNotNull;

        ParamType(String name, boolean isNotNull) {
            this.name = name;
            this.isNotNull = isNotNull;
        }

        static ParamType of(String name) {
            for (ParamType type : ParamType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
