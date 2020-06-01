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

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.nifi.web.api.dto.TemplateDTO;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "templateConfigurationEntity")
public class TemplateApplicationEntity extends Entity {

    private String id;
    private String name;
    private String description;

    private TemplateDTO template;

    @ApiModelProperty(value = "The id of the application.")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value = "The name of the application.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "The description for the application")
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * The TemplateDTO that is being serialized.
     *
     * @return The TemplateDTO object
     */
    public TemplateDTO getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDTO template) {
        this.template = template;
    }
}