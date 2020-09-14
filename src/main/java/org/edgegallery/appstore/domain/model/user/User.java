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

package org.edgegallery.appstore.domain.model.user;

import java.util.List;
import org.apache.commons.lang.Validate;

public class User {

    public static final String USER_INDEX = "userIndex";

    private String userId;

    private String userName;

    private List<Permissions> permissions;

    private String company;

    private String gender;

    private String telephone;

    /**
     * Constructor of User.
     * @param userId user id.
     * @param userName user name.
     */
    public User(String userId, String userName) {
        Validate.notNull(userId, "UserId is required");
        Validate.notNull(userId, "UserName is required");
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * Constructor of User with full parameters..
     */
    public User(String userId, String userName, List<Permissions> permissions, String company, String gender, String
        telephone) {
        this.userId = userId;
        this.userName = userName;
        this.permissions = permissions;
        this.company = company;
        this.gender = gender;
        this.telephone = telephone;
    }

    public List<Permissions> getPermissions() {
        return permissions;
    }

    public String getCompany() {
        return company;
    }

    public String getGender() {
        return gender;
    }

    public String getTelephone() {
        return telephone;
    }

    public boolean check() {
        return true;
    }
}
