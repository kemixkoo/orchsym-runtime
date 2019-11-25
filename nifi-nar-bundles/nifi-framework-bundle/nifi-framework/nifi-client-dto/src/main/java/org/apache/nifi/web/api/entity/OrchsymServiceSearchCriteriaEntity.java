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

import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * @author weiwei.zhan
 * A serialized representation of this class represents a search criteria when performing a service search.
 */
public class OrchsymServiceSearchCriteriaEntity {
    private String text = "";
    private Set<OrchsymServiceState> states = new HashSet<>();
    private Set<String> scopes = new HashSet<>();
    private String sortedField = OrchsymServiceSortField.NAME.name();
    private boolean desc;
    private boolean deleted;
    private int page = 1;
    private int pageSize = 10;

    @ApiModelProperty(value = "The search string")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @ApiModelProperty(value = "The Controller Service state")
    public Set<OrchsymServiceState> getStates() {
        return states;
    }

    public void setStates(Set<OrchsymServiceState> states) {
        this.states = states;
    }

    @ApiModelProperty(value = "The Controller Service scope")
    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getSortedField() {
        return sortedField;
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    @ApiModelProperty(value = "Whether sort in ascending order")
    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    @ApiModelProperty(value = "If true, only Controller Services that are logically deleted will be returned. " +
            "Otherwise, return Controller Services that are not logically deleted.")
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @ApiModelProperty(value = "The page number, one-based, default 1")
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @ApiModelProperty(value = "The page size, default 10")
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public enum  OrchsymServiceState {
        DISABLED,
        DISABLING,
        ENABLING,
        ENABLED,
        VALID,
        INVALID,
        VALIDATING
    }

    public enum OrchsymServiceSortField {
        NAME,
        TYPE,
        REFERENCING_COMPONENTS,
        NONE
    }
}




