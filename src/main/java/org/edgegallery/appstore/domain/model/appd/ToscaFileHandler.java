package org.edgegallery.appstore.domain.model.appd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ToscaFileHandler implements IAppdFile {

    private List<Class<?>> contextEnums = new ArrayList<>();

    private List<String> firstTypes = new ArrayList<>();

    private List<IParamsHandler> paramsHandlerList;

    @Override
    public boolean formatCheck() {
        for (IParamsHandler paramsHandler : paramsHandlerList) {
            if (!paramsHandler.checkParams()) {
                return false;
            }
        }
        return true;
    }


    public boolean delFileDescByName(IAppdContentEnum type, String name) {
        return paramsHandlerList.removeIf(
            item -> item.getFirstData().getKey().equals(type.getName()) && item.getFirstData().getValue().equals(name));
    }

    public void load(File file) {
        paramsHandlerList = new ArrayList<>();
        List<String> lines = readFileToList(file);
        if (lines.size() <= 0) {
            return;
        }
        IParamsHandler paramsHandler = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            IParamsHandler nextParamsHandler = paresFlag(line);
            if (nextParamsHandler != null) {
                if (paramsHandler != null) {
                    paramsHandlerList.add(paramsHandler);
                }
                paramsHandler = nextParamsHandler;
            }
            paramsHandler.setData(parseThisLine(line));
        }
        if (paramsHandler != null) {
            paramsHandlerList.add(paramsHandler);
        }
    }

    @Override
    public List<IParamsHandler> getParamsHandlerList() {
        return paramsHandlerList;
    }

    public String toString() {
        List<String> allData = new ArrayList<>();
        paramsHandlerList.forEach(item -> allData.add(item.toString()));
        return StringUtils.join(allData, "\n\n");
    }

    private Map.Entry<String, String> parseThisLine(String line) {
        int splitIndex = line.indexOf(":");
        String key = line.substring(0, splitIndex).trim();
        String value = line.substring(splitIndex + 1).trim();
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private List<String> readFileToList(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

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
                return new AppdFileContentHandler(contextEnums.get(i));
            }
        }
        return null;
    }

}
