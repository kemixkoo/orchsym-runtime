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
package org.apache.nifi.web.api.entity;

import java.util.Set;

/**
 * @author liuxun
 * @apiNote 封装选择的连接及其processGroup
 */
public class QueueSnippetEntity extends Entity {
    /**
     * 搜集选中的外部所有连接的ID
     */
    private Set<String> connectionIds;

    /**
     * 搜集选中的所有ProcessGroup
     */
    private Set<String> processGroupIds;

    /**
     * 是否确认断开连接
     */
    private Boolean disconnectedNodeAcknowledged;

    public Set<String> getConnectionIds() {
        return connectionIds;
    }

    public void setConnectionIds(Set<String> connectionIds) {
        this.connectionIds = connectionIds;
    }

    public Set<String> getProcessGroupIds() {
        return processGroupIds;
    }

    public void setProcessGroupIds(Set<String> processGroupIds) {
        this.processGroupIds = processGroupIds;
    }

    public Boolean getDisconnectedNodeAcknowledged() {
        return disconnectedNodeAcknowledged;
    }

    public void setDisconnectedNodeAcknowledged(Boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
    }
}
