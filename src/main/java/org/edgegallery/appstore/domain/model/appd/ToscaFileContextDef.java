package org.edgegallery.appstore.domain.model.appd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToscaFileContextDef implements IParamsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaLinkFileDef.class);

    private final Map<IAppdContextDef, String> params = new LinkedHashMap<>();

    private final Class<?> contextEnum;

    ToscaFileContextDef(Class<?> contextEnum) {
        this.contextEnum = contextEnum;
    }

    @Override
    public void setData(Map.Entry<String, String> data) {
        try {
            Object[] objects = contextEnum.getEnumConstants();
            Method valueOf = contextEnum.getMethod("of", String.class);
            Object def = valueOf.invoke(objects[0], data.getKey());
            if (def != null) {
                params.put((IAppdContextDef) def, data.getValue());
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed to invoke method 'of' from class {}", contextEnum.getName());
        }
    }

    @Override
    public boolean checkParams() {
        for (Object type : contextEnum.getEnumConstants()) {
            if (type instanceof IAppdContextDef) {
                IAppdContextDef appdContextDef = (IAppdContextDef) type;
                if (appdContextDef.isNotNull() && !params.containsKey(appdContextDef.getName())) {
                    LOGGER.info("not include param {} in the MF file.", appdContextDef.getName());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        params.forEach((key, value) -> lines.add(String.format("%s: %s", key.getName(), value).trim()));
        return StringUtils.join(lines, "\n");
    }

    @Override
    public Map.Entry<String, String> getData(int index) {
        return null;
    }

    public Map.Entry<String, String> getFirstData() {
        for (Map.Entry<IAppdContextDef, String> entry : params.entrySet()) {
            return new AbstractMap.SimpleEntry<>(entry.getKey().getName(), entry.getValue());
        }
        return null;
    }

    // @Override
    // public IAppdContextDef getStartType() {
    //     Object obj = contextEnum.getEnumConstants()[0];
    //     return (IAppdContextDef) obj;
    // }
}
