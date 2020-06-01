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

import javax.xml.bind.annotation.XmlType;

import org.apache.nifi.web.api.dto.ProcessorConfigDTO;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author weiwei.zhan
 */
@XmlType(name = "components")
public class TemplateConfigComponentEntity {
    private String id;
    private String name;
    private ProcessorConfigDTO config;

    @ApiModelProperty(value = "The id of the component.")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "The component name.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "The configuration details for the component. These details will be included in a response if the verbose flag is included in a request.")
    public ProcessorConfigDTO getConfig() {
        return config;
    }

    public void setConfig(ProcessorConfigDTO config) {
        this.config = config;
    }
}