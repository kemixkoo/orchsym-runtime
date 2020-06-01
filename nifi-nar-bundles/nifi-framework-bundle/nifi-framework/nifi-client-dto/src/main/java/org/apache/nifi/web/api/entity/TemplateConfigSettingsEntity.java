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
import org.apache.nifi.web.api.dto.VariableDTO;

import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author weiwei.zhan
 */
@XmlType(name = "settings")
public class TemplateConfigSettingsEntity {
    private Set<TemplateConfigComponentEntity> components;
    private Set<TemplateConfigComponentEntity> services;
    private Map<String, String> variables;

    @ApiModelProperty(value = "The processors for the application.")
    public Set<TemplateConfigComponentEntity> getComponents() {
        return components;
    }

    public void setComponents(Set<TemplateConfigComponentEntity> components) {
        this.components = components;
    }

    @ApiModelProperty(value = "The services for the application.")
    public Set<TemplateConfigComponentEntity> getServices() {
        return services;
    }

    public void setServices(Set<TemplateConfigComponentEntity> services) {
        this.services = services;
    }

    @ApiModelProperty(value = "The variables for the application.")
    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Set<VariableEntity> variablesToVariableEntities() {
        if (variables == null || variables.keySet().size() == 0) {
            return null;
        }
        Set<VariableEntity> variableEntitySet = new HashSet<>();
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            VariableDTO variableDTO = new VariableDTO();
            variableDTO.setName(variable.getKey());
            variableDTO.setValue(variable.getValue());
            VariableEntity variableEntity = new VariableEntity();
            variableEntity.setVariable(variableDTO);
            variableEntitySet.add(variableEntity);
        }
        return variableEntitySet;
    }
}