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

package org.edgegallery.appstore.domain.shared.exceptions;

import org.edgegallery.appstore.domain.shared.Entity;
import org.edgegallery.appstore.domain.shared.ErrorMessage;

public class EntityNotFoundException extends DomainException {

    private static final long serialVersionUID = 5224743617068936039L;

    private ErrorMessage errMsg;

    /**
     * construct with message.
     *
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * construct with message and ret.
     *
     */
    public EntityNotFoundException(String message, int ret) {
        super(message);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * construct with class, id and ret.
     *
     */
    public <T extends Entity> EntityNotFoundException(Class<T> entityClass, String id, int ret) {
        super("cannot find the " + entityClass.getSimpleName().toLowerCase() + " with id " + id);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * get error message.
     *
     */
    public ErrorMessage getErrMsg() {
        return errMsg;
    }
}
