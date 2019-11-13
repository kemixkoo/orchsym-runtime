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

package org.apache.nifi.web.api.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * @author weiwei.zhan
 * Details of a application copy request.
 */
@XmlType(name = "appCopy")
public class AppCopyDTO {
    private String appId;
    private String name;
    private String comments;
    private Set<String> tags;
    private PositionDTO position;

    /**
     * @return  The source application id
     */
    @ApiModelProperty(
            value = "The id of source application id."
    )
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * The name of the application.
     *
     * @return The name of this application
     */
    @ApiModelProperty(
            value = "The name of the application."
    )
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return comments for this application
     */
    @ApiModelProperty(
            value = "The comments for the application."
    )
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @return  The tags for the this application
     */
    @ApiModelProperty(value = "The tags of the application.")
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * The position of this application in the UI if applicable, null otherwise.
     *
     * @return The position
     */
    @ApiModelProperty(
            value = "The position of this application in the UI if applicable."
    )
    public PositionDTO getPosition() {
        return position;
    }

    public void setPosition(PositionDTO position) {
        this.position = position;
    }
}
