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
import org.apache.nifi.web.api.dto.FlowSnippetDTO;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "templateConfigurationEntity")
public class TemplateConfigurationEntity extends Entity{
    private String applicationName;
    private String applicationDesc;
    private String templateName;
    private String encodingVersion;
    private FlowSnippetDTO snippet;
    private Boolean disconnectedNodeAcknowledged;
    private TemplateConfigurationSettingsEntity settings;

    @ApiModelProperty(
            value = "The name of the application."
    )
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @ApiModelProperty(
            value = "The description for the application"
    )
    public String getApplicationDesc() {
        return applicationDesc;
    }

    public void setApplicationDesc(String applicationDesc) {
        this.applicationDesc = applicationDesc;
    }

    @ApiModelProperty(
            value = "The name of the template."
    )
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }


    @ApiModelProperty(
            value = "The encoding version of the flow snippet. If not specified, this is automatically "
                    + "populated by the node receiving the user request. If the snippet is specified, the version "
                    + "will be the latest. If the snippet is not specified, the version will come from the underlying "
                    + "template. These details need to be replicated throughout the cluster to ensure consistency."
    )
    public String getEncodingVersion() {
        return encodingVersion;
    }

    public void setEncodingVersion(String encodingVersion) {
        this.encodingVersion = encodingVersion;
    }

    @ApiModelProperty(
            value = "A flow snippet of the template contents. If not specified, this is automatically "
                    + "populated by the node receiving the user request. These details need to be replicated "
                    + "throughout the cluster to ensure consistency."
    )
    public FlowSnippetDTO getSnippet() {
        return snippet;
    }

    public void setSnippet(FlowSnippetDTO snippet) {
        this.snippet = snippet;
    }

    @ApiModelProperty(
            value = "Acknowledges that this node is disconnected to allow for mutable requests to proceed."
    )
    public Boolean isDisconnectedNodeAcknowledged() {
        return disconnectedNodeAcknowledged;
    }

    @ApiModelProperty(
            value = "Acknowledges that this node is disconnected to allow for mutable requests to proceed."
    )
    public Boolean getDisconnectedNodeAcknowledged() {
        return disconnectedNodeAcknowledged;
    }

    public void setDisconnectedNodeAcknowledged(Boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
    }

    @ApiModelProperty(
            value = "Settings used to update the application created by the template."
    )
    public TemplateConfigurationSettingsEntity getSettings() {
        return settings;
    }

    public void setSettings(TemplateConfigurationSettingsEntity settings) {
        this.settings = settings;
    }
}