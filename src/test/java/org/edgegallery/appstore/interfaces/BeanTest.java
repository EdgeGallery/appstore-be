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

package org.edgegallery.appstore.interfaces;

import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.junit.Assert;
import org.junit.Test;

public class BeanTest {

    @Test
    public void checkFile1() {
        boolean bn = false;
        String fileName1 = ("testfile:getAppPackagesSuccess.txt");
        String fileName2 = ("");
        String fileName3 = ("test\file:getAppPackagesSuccess.txt");
        String fileName4 = ("test/file:getAppPackagesSuccess.txt");
        String fileName5 = ("a3d13969-f86f-4c58879f--25df83908db4nginx.png");

        FileChecker.checkByPath(fileName1);

        try {
            FileChecker.checkByPath(fileName2);
        } catch (IllegalArgumentException e) {
            bn = true;
        }
        Assert.assertTrue(bn);

        bn = false;
        try {
            FileChecker.checkByPath(fileName3);
        } catch (IllegalArgumentException e) {
            bn = true;
        }
        Assert.assertTrue(bn);

        bn = false;
        try {
            FileChecker.checkByPath(fileName4);
        } catch (IllegalArgumentException e) {
            bn = true;
        }
        Assert.assertTrue(bn);
        try {
            FileChecker.checkByPath(fileName5);
        } catch (IllegalArgumentException e) {
            bn = true;
        }
        Assert.assertTrue(bn);
    }

}
