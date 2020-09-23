/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.RedundantCommentsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionConvert {

    /**
     * Handle Exception
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RestReturn defaultException(HttpServletRequest request, Exception e) {
        return RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR
            .getStatusCode()).error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage()).
                path(request.getRequestURI()).build();
    }

    /**
     * Handler IllegalArgumentException
     * @param e
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public RestReturn illegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        return RestReturn.builder().code(Response.Status.BAD_REQUEST
                .getStatusCode()).error(Response.Status.BAD_REQUEST.getReasonPhrase()).message(e.getMessage()).
                    path(request.getRequestURI()).build();
    }

    /**
     * Handler AccessDeniedException
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public RestReturn accessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        return RestReturn.builder().code(Response.Status.UNAUTHORIZED
                .getStatusCode()).error(Response.Status.UNAUTHORIZED.getReasonPhrase()).message(e.getMessage()).
                     path(request.getRequestURI()).build();
    }

    /**
     * Handle RuntimeException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public RestReturn runtimeException(HttpServletRequest request, RuntimeException e) {
        return RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.
                getStatusCode()).error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage()).
                        path(request.getRequestURI()).build();
    }

    /**
     * Handle ConstraintViolationException
     * @param e
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public RestReturn constraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        return RestReturn.builder().code(Response.Status.BAD_REQUEST.getStatusCode())
                .error(Response.Status.BAD_REQUEST.getReasonPhrase()).message(e.getMessage()).
                        path(request.getRequestURI()).build();
    }

    /**
     * Handle EntityNotFoundException
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseBody
    public RestReturn entityNotFoundException(HttpServletRequest request, EntityNotFoundException e) {
        return RestReturn.builder().code(Response.Status.NOT_FOUND.getStatusCode())
                .error(Response.Status.NOT_FOUND.getReasonPhrase()).message(e.getMessage()).
                        path(request.getRequestURI()).build();

    }

    /**
     * Handle UnknownReleaseExecption
     * @param e
     * @return
     */
    @ExceptionHandler(value = UnknownReleaseExecption.class)
    @ResponseBody
    public RestReturn unknownReleaseExecption(HttpServletRequest request, UnknownReleaseExecption e) {
        return RestReturn.builder().code(Response.Status.NOT_FOUND.getStatusCode())
                .error(Response.Status.NOT_FOUND.getReasonPhrase()).message(e.getMessage()).
                        path(request.getRequestURI()).build();
    }

    /**
     * Handle RedundantCommentsException
     * @param e
     * @return
     */
    @ExceptionHandler(value = RedundantCommentsException.class)
    @ResponseBody
    public RestReturn redundantCommentsException(HttpServletRequest request, RedundantCommentsException e) {
        return RestReturn.builder().code(Response.Status.BAD_REQUEST.getStatusCode())
                .error(Response.Status.BAD_REQUEST.getReasonPhrase()).message(e.getMessage()).
                        path(request.getRequestURI()).build();
    }
}