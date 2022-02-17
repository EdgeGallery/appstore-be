/*
 * Copyright 2022 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.controlleradvice;

import com.google.gson.Gson;
import java.lang.reflect.Method;
import java.util.Locale;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspectHandler {

    @Pointcut("@annotation(org.edgegallery.appstore.interfaces.controlleradvice.LogReturning)")
    public void logPointCut() {
    }

    /**
     * record return info into log file.
     *
     * @param joinPoint join point
     * @param returnValue return value
     */
    @AfterReturning(value = "logPointCut()", returning = "returnValue")
    public void saveReturn(JoinPoint joinPoint, Object returnValue) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogReturning logReturning = method.getAnnotation(LogReturning.class);
        String level = logReturning.level();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        switch (level.toLowerCase(Locale.ROOT)) {
            case "error":
                logger.error("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
                return;
            case "warn":
                logger.warn("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
                return;
            case "info":
            default:
                logger.info("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
        }
    }
}
