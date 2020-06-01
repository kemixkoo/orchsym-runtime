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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author weiwei.zhan
 * A serialized representation of this class can be placed in the entity body of a request or response to or from the API.
 */
@XmlRootElement(name = "controllerServiceCutEntity")
public class ControllerServiceMoveEntity extends Entity {
    private String name;
    private String comments;
    private String groupId;
    private Boolean disconnectedNodeAcknowledged;

    @ApiModelProperty("The id of the Target Process Group")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return controller service name
     */
    @ApiModelProperty(
            value = "The name of the controller service."
    )
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the comment for the Controller Service
     */
    @ApiModelProperty(
            value = "The comments for the controller service."
    )
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    @ApiModelProperty(
            value = "Acknowledges that this node is disconnected to allow for mutable requests to proceed."
    )
    public Boolean isDisconnectedNodeAcknowledged() {
        return disconnectedNodeAcknowledged;
    }

    public void setDisconnectedNodeAcknowledged(Boolean disconnectedNodeAcknowledged) {
        this.disconnectedNodeAcknowledged = disconnectedNodeAcknowledged;
    }
}
