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
package org.apache.nifi.web.api.orchsym.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.nifi.web.api.orchsym.OrchsymSearchEntity;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author weiwei.zhan
 *         A serialized representation of this class represents a search criteria when performing a service search.
 */
public class OrchsymServiceSearchCriteriaEntity extends OrchsymSearchEntity {
    private Set<OrchsymServiceState> states = new HashSet<>();
    private Set<String> scopes = new HashSet<>();
    private String sortedField = OrchsymServiceSortField.NAME.name();

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

    public enum OrchsymServiceState {
        DISABLED, DISABLING, ENABLING, ENABLED, VALID, INVALID, VALIDATING
    }

    public enum OrchsymServiceSortField {
        NAME, TYPE, REFERENCING_COMPONENTS, NONE
    }
}
