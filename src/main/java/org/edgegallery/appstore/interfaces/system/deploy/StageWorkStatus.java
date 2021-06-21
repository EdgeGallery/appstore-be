/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.system.deploy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.edgegallery.appstore.domain.model.system.lcm.EnumTestConfigStatus;
import org.edgegallery.appstore.domain.model.system.vm.PodEvents;
import org.edgegallery.appstore.domain.model.system.vm.PodEventsRes;
import org.edgegallery.appstore.domain.model.system.vm.PodStatusInfo;
import org.edgegallery.appstore.domain.model.system.vm.PodStatusInfos;
import org.edgegallery.appstore.infrastructure.util.HttpClientUtil;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * StageWorkStatus.
 *
 * @author chenhui
 */
@Service("workStatus_service")
public class StageWorkStatus implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageWorkStatus.class);

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 360L;

    private static final Gson gson = new Gson();

    @Autowired
    private ProjectService projectService;

    @Override
    public boolean execute(Map<String, String> hostMap) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;

        // ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = hostMap.get("userId");
        String protocol = hostMap.get("protocol");
        int port = Integer.parseInt((hostMap.get("port")));
        String lcmIp = hostMap.get("lcmIp");
        String token = hostMap.get("token");
        String packageId = hostMap.get("packageId");

        // Type type = new TypeToken<List<MepHost>>() { }.getType();
        // List<MepHost> hosts = gson.fromJson(gson.toJson(config.getHosts()), type);
        // MepHost host = hosts.get(0);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        long time1 = System.currentTimeMillis();
        String workStatus = HttpClientUtil.getWorkloadStatus(protocol, lcmIp, port, packageId, userId, token);
        LOGGER.info("pod workStatus: {}", workStatus);
        String workEvents = HttpClientUtil.getWorkloadEvents(protocol, lcmIp, port, packageId, userId, token);
        LOGGER.info("pod workEvents: {}", workEvents);
        if (workStatus == null || workEvents == null) {
            // compare time between now and deployDate
            return false;
        } else {
            processStatus = true;
        }
        return processStatus;
    }

    private String mergeStatusAndEvents(String workStatus, String workEvents) {
        Gson gson = new Gson();
        Type type = new TypeToken<PodStatusInfos>() { }.getType();
        PodStatusInfos status = gson.fromJson(workStatus, type);

        Type typeEvents = new TypeToken<PodEventsRes>() { }.getType();
        PodEventsRes events = gson.fromJson(workEvents, typeEvents);
        String pods = "";
        if (!CollectionUtils.isEmpty(status.getPods()) && !CollectionUtils.isEmpty(events.getPods())) {
            List<PodStatusInfo> statusInfos = status.getPods();
            List<PodEvents> eventsInfos = events.getPods();
            for (int i = 0; i < statusInfos.size(); i++) {
                for (int j = 0; j < eventsInfos.size(); j++) {
                    if (statusInfos.get(i).getPodname().equals(eventsInfos.get(i).getPodName())) {
                        statusInfos.get(i).setPodEventsInfo(eventsInfos.get(i).getPodEventsInfo());
                    }
                }
            }
            pods = gson.toJson(status);
        }
        return pods;
    }

    // @Override
    // public boolean destroy() {
    //     return true;
    // }

    // @Override
    // public boolean immediateExecute(ProjectTestConfig config) {
    //     return true;
    // }
}
