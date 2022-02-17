/*
 *    Copyright 2020-2022 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.interfaces.controlleradvice;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.edgegallery.appstore.domain.shared.exceptions.PermissionNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingMatrixVariableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
public class GlobalExceptionConvert {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionConvert.class);

    private static final Gson GSON = new Gson();

    /**
     * Handle Exception.
     *
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RestReturn defaultException(HttpServletRequest request, Exception e) {
        if (e instanceof MissingMatrixVariableException || e instanceof HttpMessageNotReadableException
            || e instanceof MethodArgumentNotValidException || e instanceof MissingPathVariableException) {
            return badRequestResponse(request, e);
        }
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(ResponseConst.RET_FAIL).params(null).build();
        LOGGER.error("defaultException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    private RestReturn badRequestResponse(HttpServletRequest request, Exception e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.BAD_REQUEST.getStatusCode())
            .error(Response.Status.BAD_REQUEST.getReasonPhrase()).message(e.getMessage()).path(request.getRequestURI())
            .retCode(ResponseConst.RET_FAIL).params(null).build();
        LOGGER.error("badRequestResponse: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handler IllegalArgumentException.
     *
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public RestReturn illegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        return badRequestResponse(request, e);
    }

    /**
     * Handler AccessDeniedException.
     *
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public RestReturn accessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.FORBIDDEN.getStatusCode())
            .error(Response.Status.FORBIDDEN.getReasonPhrase()).message(e.getMessage()).path(request.getRequestURI())
            .retCode(ResponseConst.RET_FAIL).params(null).build();
        LOGGER.error("accessDeniedException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle RuntimeException.
     *
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public RestReturn runtimeException(HttpServletRequest request, RuntimeException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(ResponseConst.RET_FAIL).params(null).build();
        LOGGER.error("runtimeException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle ConstraintViolationException.
     *
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public RestReturn constraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        return badRequestResponse(request, e);
    }

    /**
     * Handle MissingServletRequestParameterException.
     *
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public RestReturn missingServletRequestParameterException(HttpServletRequest request,
        MissingServletRequestParameterException e) {
        return badRequestResponse(request, e);
    }

    /**
     * Handle HttpMessageNotReadableException.
     *
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public RestReturn httpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        return badRequestResponse(request, e);
    }

    /**
     * Handle MissingServletRequestPartException.
     *
     */
    @ExceptionHandler(value = MissingServletRequestPartException.class)
    @ResponseBody
    public RestReturn missingServletRequestPartException(HttpServletRequest request,
        MissingServletRequestPartException e) {
        return badRequestResponse(request, e);
    }

    /**
     * Handle EntityNotFoundException.
     *
     */
    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseBody
    public RestReturn entityNotFoundException(HttpServletRequest request, EntityNotFoundException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(e.getErrMsg().getRetCode())
            .params(e.getErrMsg().getParams()).build();
        LOGGER.error("entityNotFoundException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle FileNotFoundException.
     *
     */
    @ExceptionHandler(value = FileNotFoundException.class)
    @ResponseBody
    public RestReturn fileNotFoundException(HttpServletRequest request, FileNotFoundException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.NOT_FOUND.getStatusCode())
            .error(Response.Status.NOT_FOUND.getReasonPhrase()).message(e.getMessage()).path(request.getRequestURI())
            .retCode(ResponseConst.RET_FAIL).params(null).build();
        LOGGER.error("fileNotFoundException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle UnknownReleaseExecption.
     *
     */
    @ExceptionHandler(value = UnknownReleaseExecption.class)
    @ResponseBody
    public RestReturn unknownReleaseException(HttpServletRequest request, UnknownReleaseExecption e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(e.getErrMsg().getRetCode())
            .params(e.getErrMsg().getParams()).build();
        LOGGER.error("unknownReleaseException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle PermissionNotAccessException.
     *
     */
    @ExceptionHandler(value = PermissionNotAllowedException.class)
    @ResponseBody
    public RestReturn permissionNotAccessException(HttpServletRequest request, PermissionNotAllowedException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.FORBIDDEN.getStatusCode())
            .error(Response.Status.FORBIDDEN.getReasonPhrase()).message(e.getMessage()).path(request.getRequestURI())
            .retCode(e.getErrMsg().getRetCode()).params(e.getErrMsg().getParams()).build();
        LOGGER.error("permissionNotAccessException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle FileOperateException.
     *
     */
    @ExceptionHandler(value = FileOperateException.class)
    @ResponseBody
    public RestReturn fileOperateException(HttpServletRequest request, FileOperateException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(e.getErrMsg().getRetCode())
            .params(e.getErrMsg().getParams()).build();
        LOGGER.error("fileOperateException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle RuntimeException.
     *
     */
    @ExceptionHandler(value = AppException.class)
    @ResponseBody
    public RestReturn appException(HttpServletRequest request, AppException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(e.getErrMsg().getRetCode())
            .params(e.getErrMsg().getParams()).build();
        LOGGER.error("appException: {}", GSON.toJson(restReturn));
        return restReturn;
    }

    /**
     * Handle RuntimeException.
     *
     */
    @ExceptionHandler(value = IllegalRequestException.class)
    @ResponseBody
    public RestReturn illegalRequestException(HttpServletRequest request, IllegalRequestException e) {
        RestReturn restReturn = RestReturn.builder().code(Response.Status.BAD_REQUEST.getStatusCode())
            .error(Response.Status.BAD_REQUEST.getReasonPhrase()).message(e.getMessage())
            .path(request.getRequestURI()).retCode(e.getErrMsg().getRetCode())
            .params(e.getErrMsg().getParams()).build();
        LOGGER.error("illegalRequestException: {}", GSON.toJson(restReturn));
        return restReturn;
    }
}