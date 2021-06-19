/* Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.domain.shared.exceptions;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.domain.shared.Entity;
import org.edgegallery.appstore.domain.shared.ErrorMessage;

public class PermissionNotAllowedException extends DomainException {

    private static final long serialVersionUID = 6824743617068936088L;

    private ErrorMessage errMsg;

    public <T extends Entity> PermissionNotAllowedException(String message) {
        super("Permission not allowed: " + message);
    }

    /**
     * Constructor to create AppException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public PermissionNotAllowedException(String msg, int ret, Object... args) {
        super(msg);
        List<String> params = new ArrayList<>();
        int length = args == null ? 0 : args.length;
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret, params);
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
