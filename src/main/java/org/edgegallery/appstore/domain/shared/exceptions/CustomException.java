/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.domain.shared.exceptions;

import org.springframework.web.client.RestClientException;

public class CustomException extends RestClientException {

    private static final long serialVersionUID = 1L;

    private RestClientException restClientException;

    private String body;

    /**
     * cus.
     *
     * @param msg msg
     * @param restClientException rest
     * @param body body
     */
    public CustomException(String msg, RestClientException restClientException, String body) {
        super(msg);
        this.restClientException = restClientException;
        this.body = body;
    }

    public RestClientException getRestClientException() {
        return restClientException;
    }

    public void setRestClientException(RestClientException restClientException) {
        this.restClientException = restClientException;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
