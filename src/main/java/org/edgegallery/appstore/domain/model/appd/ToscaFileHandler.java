package org.edgegallery.appstore.domain.model.appd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ToscaFileHandler extends AToscaFileHandler {

    private List<Class<?>> contextEnums = new ArrayList<>();

    private List<String> firstTypes = new ArrayList<>();

    ToscaFileHandler(Class<?>... def) {
        try {
            for (Class<?> aClass : def) {
                contextEnums.add(aClass);
                Object[] objects = aClass.getEnumConstants();
                Method getName = aClass.getMethod("getName");
                firstTypes.add((String) getName.invoke(objects[0]));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    IParamsHandler paresFlag(String line) {
        for (int i = 0; i < firstTypes.size(); i++) {
            if (line.startsWith(firstTypes.get(i))) {
                return new ToscaFileContextDef(contextEnums.get(i));
            }
        }
        return null;
    }

    @Override
    public boolean delFileDescByName(String name) {
        return false;
    }

    // @Override
    // public boolean delFileDescByName(String name) {
    //     return false;
    // }
}
