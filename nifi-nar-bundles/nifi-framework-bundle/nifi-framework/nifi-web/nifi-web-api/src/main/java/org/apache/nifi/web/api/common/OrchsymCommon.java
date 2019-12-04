/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.web.api.common;

import org.apache.nifi.connectable.Connection;
import org.apache.nifi.controller.AbstractComponentNode;
import org.apache.nifi.controller.ComponentNode;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.web.api.orchsym.group.AllComponentIdInGroup;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 为Resource 封装常用的方法
 *
 * @author liuxun
 */
public class OrchsymCommon {
    private FlowController flowController;

    /**
     * 获取指定ProcessGroup中的所有处理器ID
     *
     * @param groupId
     * @return
     */
    public Set<String> getAllProcessorIdsInGroup(String groupId) {
        return getAllComponentIdsByType(groupId, CollectIdType.COLLECT_PROCESSOR_ID).getProcessorIds();
    }

    public Set<String> getAllServiceIdsInGroup(String groupId) {
        return getAllComponentIdsByType(groupId, CollectIdType.COLLECT_SERVICE_ID).getServiceIds();
    }

    public Set<String> getAllConnectionIdsInGroup(String groupId) {
        return getAllComponentIdsByType(groupId, CollectIdType.COLLECT_CONNECTION_ID).getConnectionIds();
    }

    public Set<String> getAllTemplateIdsInGroup(String groupId) {
        return getAllComponentIdsByType(groupId, CollectIdType.COLLECT_TEMPLATE_ID).getTemplateIds();
    }

    public AllComponentIdInGroup getAllComponentIdsInGroup(String groupId){
        return getAllComponentIdsByType(groupId, CollectIdType.COLLECT_ALL_ID);

    }

    private AllComponentIdInGroup getAllComponentIdsByType(String groupId, CollectIdType type){
        boolean includeProcessor = false;
        boolean includeService = false;
        boolean includeConnection = false;
        boolean includeTemplate = false;
        switch (type){
            case COLLECT_ALL_ID:
                includeProcessor = true;
                includeService = true;
                includeConnection = true;
                includeTemplate = true;
                break;
            case COLLECT_PROCESSOR_ID:
                includeProcessor = true;
                break;
            case COLLECT_SERVICE_ID:
                includeService = true;
                break;
            case COLLECT_CONNECTION_ID:
                includeConnection = true;
                break;
            case COLLECT_TEMPLATE_ID:
                includeTemplate = true;
                break;
            default:
                break;
        }

        return getDirectComponentIdsInGroup(groupId,includeProcessor,includeService,includeConnection,includeTemplate);
    }

    /**
     * 按照要求返回所有指定类型的组件ID
     * @param groupId
     * @param includeProcessor
     * @param includeService
     * @param includeConnection
     * @param includeTemplate
     * @return
     */
    private AllComponentIdInGroup getDirectComponentIdsInGroup(
            String groupId,
            boolean includeProcessor,
            boolean includeService,
            boolean includeConnection,
            boolean includeTemplate) {
        ProcessGroup group = flowController.getGroup(groupId);
        if (group == null) {
            return null;
        }
        Set<String> processorIds = includeProcessor ? new HashSet<>() : null;
        Set<String> serviceIds = includeService ? new HashSet<>() : null;
        Set<String> connectionIds = includeConnection ? new HashSet<>() : null;
        Set<String> templateIds = includeTemplate ? new HashSet<>() : null;

        getIds(group, processorIds, serviceIds, connectionIds, templateIds);

        return new AllComponentIdInGroup(processorIds,serviceIds,connectionIds,templateIds);
    }


    /**
     * 递归获取所有的ID
     */
    private void getIds(ProcessGroup group,
                        Set<String> processorIds,
                        Set<String> serviceIds,
                        Set<String> connectionIds,
                        Set<String> templateIds) {
        if (processorIds != null) {
            Set<String> idsSet = group.getProcessors().stream().map(AbstractComponentNode::getIdentifier).collect(Collectors.toSet());
            processorIds.addAll(idsSet);
        }

        if (serviceIds != null) {
            Set<String> sIdsSet = group.getControllerServices(false).stream().map(ComponentNode::getIdentifier).collect(Collectors.toSet());
            serviceIds.addAll(sIdsSet);
        }

        if (connectionIds != null) {
            Set<String> cSet = group.getConnections().stream().map(Connection::getIdentifier).collect(Collectors.toSet());
            connectionIds.addAll(cSet);
        }

        if (templateIds != null) {
            Set<String> tSet = group.getTemplates().stream().map(t -> t.getIdentifier()).collect(Collectors.toSet());
            templateIds.addAll(tSet);
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            getIds(childGroup, processorIds, serviceIds, connectionIds, templateIds);
        }
    }

    private enum CollectIdType {
        /**
         *  搜集的ID的组件类型
         */
        COLLECT_PROCESSOR_ID,
        COLLECT_SERVICE_ID,
        COLLECT_CONNECTION_ID,
        COLLECT_TEMPLATE_ID,
        COLLECT_ALL_ID
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }
}
