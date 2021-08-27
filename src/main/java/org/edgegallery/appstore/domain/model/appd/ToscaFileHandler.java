/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.appstore.domain.model.appd;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToscaFileHandler implements IAppdFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaFileHandler.class);

    private final List<Class<?>> contextEnums = new ArrayList<>();

    private final List<String> firstTypes = new ArrayList<>();

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

    public boolean delContentByTypeAndValue(IAppdContentEnum type, String name) {
        return paramsHandlerList.removeIf(
            item -> item.getFirstData().getKey().equals(type.getName()) && item.getFirstData().getValue().equals(name));
    }

    /**
     * to load file.
     */
    public void load(File file) {
        paramsHandlerList = new ArrayList<>();
        List<String> lines = getLines(file);
        if (lines == null || lines.size() <= 0) {
            return;
        }
        // split list by empty line
        List<List<String>> splitLines = splitByEmptyLine(lines);
        for (List<String> lineRange : splitLines) {
            IParamsHandler paramsHandler = paresFlag(lineRange.get(0));
            if (paramsHandler == null) {
                LOGGER.info("this data {} not define in the class {}", lineRange.get(0), contextEnums);
                continue;
            }
            for (String s : lineRange) {
                paramsHandler.addOneData(parseThisLine(s));
            }
            paramsHandlerList.add(paramsHandler);
        }
    }

    private List<List<String>> splitByEmptyLine(List<String> lines) {
        List<List<String>> splitLines = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        for (String line : lines) {
            if (!StringUtils.isEmpty(line)) {
                temp.add(line);
            } else {
                if (!temp.isEmpty()) {
                    splitLines.add(temp);
                }
                temp = new ArrayList<>();
            }
        }
        if (!temp.isEmpty()) {
            splitLines.add(temp);
        }
        return splitLines;
    }

    private List<String> getLines(File file) {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("failed to read file {}", file.getPath());
            return null;
        }
    }

    /**
     * get the params after load file.
     */
    @Override
    public List<IParamsHandler> getParamsHandlerList() {
        return paramsHandlerList;
    }

    /**
     * to string.
     */
    public String toString() {
        List<String> allData = new ArrayList<>();
        paramsHandlerList.forEach(item -> allData.add(item.toString()));
        return StringUtils.join(allData, "\n\n");
    }

    private Map.Entry<String, String> parseThisLine(String line) {
        int splitIndex = line.indexOf(":");
        if (splitIndex > 0) {
            String key = line.substring(0, splitIndex).trim();
            String value = line.substring(splitIndex + 1).trim();
            return new AbstractMap.SimpleEntry<>(key, value);
        } else {
            String key = line;
            String value = line;
            return new AbstractMap.SimpleEntry<>(key, value);
        }
    }

    ToscaFileHandler(Class<?>... def) {
        try {
            for (Class<?> clz : def) {
                contextEnums.add(clz);
                Object[] objects = clz.getEnumConstants();
                Method getName = clz.getMethod("getName");
                firstTypes.add((String) getName.invoke(objects[0]));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("failed to invoke method in Class.");
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
