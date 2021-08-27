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

public class AppdFileContentHandler implements IParamsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppdFileContentHandler.class);

    private final Map<IAppdContentEnum, String> params = new LinkedHashMap<>();

    private final Class<?> contextEnum;

    AppdFileContentHandler(Class<?> contextEnum) {
        this.contextEnum = contextEnum;
    }

    @Override
    public void setData(Map.Entry<String, String> data) {
        try {
            Object[] objects = contextEnum.getEnumConstants();
            Method valueOf = contextEnum.getMethod("of", String.class);
            Object def = valueOf.invoke(objects[0], data.getKey());
            if (def != null) {
                params.put((IAppdContentEnum) def, data.getValue());
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed to invoke method 'of' from class {}", contextEnum.getName());
        }
    }

    @Override
    public boolean checkParams() {
        for (Object type : contextEnum.getEnumConstants()) {
            if (type instanceof IAppdContentEnum) {
                IAppdContentEnum appdContextDef = (IAppdContentEnum) type;
                if (appdContextDef.isNotNull() && !params.containsKey(appdContextDef)) {
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

    public Map.Entry<String, String> getFirstData() {
        IAppdContentEnum contentEnum = (IAppdContentEnum) contextEnum.getEnumConstants()[0];
        return new AbstractMap.SimpleEntry<>(contentEnum.getName(), params.get(contentEnum));
    }
}
