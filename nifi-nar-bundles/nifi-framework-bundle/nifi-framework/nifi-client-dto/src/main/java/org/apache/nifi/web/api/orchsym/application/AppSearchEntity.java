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
package org.apache.nifi.web.api.orchsym.application;

import java.util.Set;

import org.apache.nifi.web.api.orchsym.OrchsymSearchEntity;

/**
 */
public class AppSearchEntity extends OrchsymSearchEntity {
    public static final String PARAM_MODIFIED_TIME = "modifiedtime";
    public static final String PARAM_CREATED_TIME = "createdtime";
    public static final String NAME_FIELD = "name";

    // 排序
    private String sortedField = NAME_FIELD; // 目前支持: name, createdTime, modifiedTime

    // 过滤
    private Boolean enabled; // 允许null，表示不过滤是否禁用，即忽略该状态
    private Boolean isRunning;
    private Boolean hasDataQueue;

    private String filterTimeField; // 目前支持: createdTime, modifiedTime
    private Long beginTime; // 起止时间
    private Long endTime;

    private Set<String> tags;

    private boolean needDetail = false; // 详情

    public String getSortedField() {
        return sortedField;
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public Boolean getHasDataQueue() {
        return hasDataQueue;
    }

    public void setHasDataQueue(Boolean hasDataQueue) {
        this.hasDataQueue = hasDataQueue;
    }

    public String getFilterTimeField() {
        return filterTimeField;
    }

    public void setFilterTimeField(String filterTimeField) {
        this.filterTimeField = filterTimeField;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean isNeedDetail() {
        return needDetail;
    }

    public void setNeedDetail(boolean needDetail) {
        this.needDetail = needDetail;
    }

}
