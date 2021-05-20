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

package org.edgegallery.appstore.domain.model.releases;

import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;

public class UnknownReleaseExecption extends DomainException {

    private static final long serialVersionUID = 8438080003774246931L;

    private ErrorMessage errMsg;

    public UnknownReleaseExecption(String packageId) {
        super("No release with packageId " + packageId + " exists in the system");
    }

    /**
     * construct with message and ret.
     *
     */
    public UnknownReleaseExecption(String packageId, int ret) {
        super("No release with packageId " + packageId + " exists in the system");
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
