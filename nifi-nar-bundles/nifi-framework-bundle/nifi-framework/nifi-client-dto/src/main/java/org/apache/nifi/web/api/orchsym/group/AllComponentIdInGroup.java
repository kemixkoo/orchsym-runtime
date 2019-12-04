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

package org.apache.nifi.web.api.orchsym.group;

import java.util.Set;

/**
 * 为Group封装所有的id
 * @author liuxun
 */
public class AllComponentIdInGroup {
    Set<String> processorIds;
    Set<String> serviceIds;
    Set<String> connectionIds;
    Set<String> templateIds;

    public AllComponentIdInGroup() {
    }

    public AllComponentIdInGroup(Set<String> processorIds, Set<String> serviceIds, Set<String> connectionIds, Set<String> templateIds) {
        this.processorIds = processorIds;
        this.serviceIds = serviceIds;
        this.connectionIds = connectionIds;
        this.templateIds = templateIds;
    }

    public Set<String> getProcessorIds() {
        return processorIds;
    }

    public void setProcessorIds(Set<String> processorIds) {
        this.processorIds = processorIds;
    }

    public Set<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(Set<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public Set<String> getConnectionIds() {
        return connectionIds;
    }

    public void setConnectionIds(Set<String> connectionIds) {
        this.connectionIds = connectionIds;
    }

    public Set<String> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(Set<String> templateIds) {
        this.templateIds = templateIds;
    }
}
