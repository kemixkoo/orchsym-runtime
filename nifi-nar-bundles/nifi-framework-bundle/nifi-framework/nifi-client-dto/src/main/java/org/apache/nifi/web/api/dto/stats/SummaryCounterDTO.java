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
package org.apache.nifi.web.api.dto.stats;

import java.util.Map;

/**
 * @author GU Guoqiang
 *
 */
public class SummaryCounterDTO {
    // 应用
    private Integer appCount;
    private Integer appRunningCount;
    private Integer appStoppedCount;

    private Long groupCount; // 所有模块总数
    private Long groupLeavesCount;
    private Long templateCount;
    private Long labelCount;
    private Long varCount;
    private Long funnelCount;
    private Integer inputPortCount;
    private Integer outputPortCount;
    private Long connectionCount;

    private Integer runningCount;
    private Integer stoppedCount;
    private Integer invalidCount;
    private Integer disabledCount;

    private Integer activeRemotePortCount;
    private Integer inactiveRemotePortCount;

    private Integer upToDateCount;
    private Integer locallyModifiedCount;
    private Integer staleCount;
    private Integer locallyModifiedAndStaleCount;
    private Integer syncFailureCount;

    private Long components; // 平台提供组件数
    private Long componentsOwned; // 平台自主研发组件数
    private Long componentsUsed; // 使用组件数
    private Long componentsUsedCount; // 使用组件频次总数
    private Map<String, Long> componentsI18n;

    private Long services; // 平台提供的服务
    private Long servicesOwned; // 自主研发服务
    private Long servicesUsed; // 使用服务数
    private Long servicesUsedCount; // 使用服务频次总数
    private Map<String, Long> servicesI18n;

    public Integer getAppCount() {
        return appCount;
    }

    public void setAppCount(Integer appCount) {
        this.appCount = appCount;
    }

    public Integer getAppRunningCount() {
        return appRunningCount;
    }

    public void setAppRunningCount(Integer appRunningCount) {
        this.appRunningCount = appRunningCount;
    }

    public Integer getAppStoppedCount() {
        return appStoppedCount;
    }

    public void setAppStoppedCount(Integer appStoppedCount) {
        this.appStoppedCount = appStoppedCount;
    }

    public Long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Long groupCount) {
        this.groupCount = groupCount;
    }

    public Long getGroupLeavesCount() {
        return groupLeavesCount;
    }

    public void setGroupLeavesCount(Long groupLeavesCount) {
        this.groupLeavesCount = groupLeavesCount;
    }

    public Long getTemplateCount() {
        return templateCount;
    }

    public void setTemplateCount(Long templateCount) {
        this.templateCount = templateCount;
    }

    public Long getLabelCount() {
        return labelCount;
    }

    public void setLabelCount(Long labelCount) {
        this.labelCount = labelCount;
    }

    public Long getVarCount() {
        return varCount;
    }

    public void setVarCount(Long varCount) {
        this.varCount = varCount;
    }

    public Long getFunnelCount() {
        return funnelCount;
    }

    public void setFunnelCount(Long funnelCount) {
        this.funnelCount = funnelCount;
    }

    public Integer getInputPortCount() {
        return inputPortCount;
    }

    public void setInputPortCount(Integer inputPortCount) {
        this.inputPortCount = inputPortCount;
    }

    public Integer getOutputPortCount() {
        return outputPortCount;
    }

    public void setOutputPortCount(Integer outputPortCount) {
        this.outputPortCount = outputPortCount;
    }

    public Long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(Long connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Integer getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    public Integer getStoppedCount() {
        return stoppedCount;
    }

    public void setStoppedCount(Integer stoppedCount) {
        this.stoppedCount = stoppedCount;
    }

    public Integer getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(Integer invalidCount) {
        this.invalidCount = invalidCount;
    }

    public Integer getDisabledCount() {
        return disabledCount;
    }

    public void setDisabledCount(Integer disabledCount) {
        this.disabledCount = disabledCount;
    }

    public Integer getActiveRemotePortCount() {
        return activeRemotePortCount;
    }

    public void setActiveRemotePortCount(Integer activeRemotePortCount) {
        this.activeRemotePortCount = activeRemotePortCount;
    }

    public Integer getInactiveRemotePortCount() {
        return inactiveRemotePortCount;
    }

    public void setInactiveRemotePortCount(Integer inactiveRemotePortCount) {
        this.inactiveRemotePortCount = inactiveRemotePortCount;
    }

    public Integer getUpToDateCount() {
        return upToDateCount;
    }

    public void setUpToDateCount(Integer upToDateCount) {
        this.upToDateCount = upToDateCount;
    }

    public Integer getLocallyModifiedCount() {
        return locallyModifiedCount;
    }

    public void setLocallyModifiedCount(Integer locallyModifiedCount) {
        this.locallyModifiedCount = locallyModifiedCount;
    }

    public Integer getStaleCount() {
        return staleCount;
    }

    public void setStaleCount(Integer staleCount) {
        this.staleCount = staleCount;
    }

    public Integer getLocallyModifiedAndStaleCount() {
        return locallyModifiedAndStaleCount;
    }

    public void setLocallyModifiedAndStaleCount(Integer locallyModifiedAndStaleCount) {
        this.locallyModifiedAndStaleCount = locallyModifiedAndStaleCount;
    }

    public Integer getSyncFailureCount() {
        return syncFailureCount;
    }

    public void setSyncFailureCount(Integer syncFailureCount) {
        this.syncFailureCount = syncFailureCount;
    }

    public Long getComponents() {
        return components;
    }

    public void setComponents(Long components) {
        this.components = components;
    }

    public Long getComponentsOwned() {
        return componentsOwned;
    }

    public void setComponentsOwned(Long componentsOwned) {
        this.componentsOwned = componentsOwned;
    }

    public Long getComponentsUsed() {
        return componentsUsed;
    }

    public void setComponentsUsed(Long componentsUsed) {
        this.componentsUsed = componentsUsed;
    }

    public Long getComponentsUsedCount() {
        return componentsUsedCount;
    }

    public void setComponentsUsedCount(Long componentsUsedCount) {
        this.componentsUsedCount = componentsUsedCount;
    }

    public Map<String, Long> getComponentsI18n() {
        return componentsI18n;
    }

    public void setComponentsI18n(Map<String, Long> componentsI18n) {
        this.componentsI18n = componentsI18n;
    }

    public Long getServices() {
        return services;
    }

    public void setServices(Long services) {
        this.services = services;
    }

    public Long getServicesOwned() {
        return servicesOwned;
    }

    public void setServicesOwned(Long servicesOwned) {
        this.servicesOwned = servicesOwned;
    }

    public Long getServicesUsed() {
        return servicesUsed;
    }

    public void setServicesUsed(Long servicesUsed) {
        this.servicesUsed = servicesUsed;
    }

    public Long getServicesUsedCount() {
        return servicesUsedCount;
    }

    public void setServicesUsedCount(Long servicesUsedCount) {
        this.servicesUsedCount = servicesUsedCount;
    }

    public Map<String, Long> getServicesI18n() {
        return servicesI18n;
    }

    public void setServicesI18n(Map<String, Long> servicesI18n) {
        this.servicesI18n = servicesI18n;
    }

}
