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

package org.edgegallery.appstore.infrastructure.util;

import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class ScheduleTask {

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 0 0 * * ? ")
    public void processCleanEnv() {
        projectService.cleanUnreleasedEnv();
    }

    @Scheduled(cron = "0 0 0 * * ? ")
    public void processCleanTempPackage() {
        packageServiceFacade.scheduledDeletePackage();
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void processUpdateQueryOrder() {
        projectService.scheduledQueryOrder();
    }

}
