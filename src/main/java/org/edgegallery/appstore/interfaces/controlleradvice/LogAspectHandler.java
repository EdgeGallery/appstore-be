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

    @AfterReturning(value = "logPointCut()", returning = "returnValue")
    public void saveReturn(JoinPoint joinPoint, Object returnValue) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogReturning logReturning = method.getAnnotation(LogReturning.class);
        String level = logReturning.level();
        Logger LOGGER = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        switch (level.toLowerCase(Locale.ROOT)) {
            case "error":
                LOGGER.error("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
                return;
            case "warn":
                LOGGER.warn("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
                return;
            case "info":
            default:
                LOGGER.info("method:{} return:{}", method.getName(), new Gson().toJson(returnValue));
        }
    }
}
