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

import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.nifi.web.api.entity.Entity;

/**
 * @author weiwei.zhan
 *
 * A serialized representation of this class can be placed in the entity body of a request or response to or from the API.
 * This particular entity holds the relevant operations on a bulk of controller services.
 */
@XmlRootElement(name = "controllerServicesBatchOperationEntity")
public class ControllerServicesBatchOperationEntity extends Entity {
    private String operation;
    private boolean includeDescendantGroups;
    private boolean skipInvalid;
    private Boolean disconnectedNodeAcknowledged;

    /**
     * The operation type.
     * @return type of the operation
     */
    @ApiModelProperty(
            value = "The operation on the controller services.",
            allowableValues = "ENABLE, DISABLE, DELETE"
    )
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Whether apply the operation to the children process groups
     * @return  true if apply the operation to the children process groups
     */
    @ApiModelProperty(
            value = "Whether apply the operation to the children process groups",
            allowableValues = "true, false"
    )
    public boolean isIncludeDescendantGroups() {
        return includeDescendantGroups;
    }

    public void setIncludeDescendantGroups(boolean includeDescendantGroups) {
        this.includeDescendantGroups = includeDescendantGroups;
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

    public boolean isSkipInvalid() {
        return skipInvalid;
    }

    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
    }

    /**
     * Represents the batch operation on a bulk of controller services.
     */
    public enum ControllerServiceBatchOperation{
        ENABLE,
        DISABLE,
        LOGICAL_DELETION,
        RECOVERY,
        PHYSICAL_DELETION;

        @Override
        public String toString() {
            return name();
        }
    }
}

