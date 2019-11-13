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
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.SnippetDTO;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A serialized representation of this class can be placed in the entity body of a request or response to or from the API.
 * @author weiwei.zhan
 */
@XmlRootElement(name = "snippetEntity")
public class SnippetCutEntity extends Entity {

    private SnippetDTO snippet;
    private PositionDTO positionOffset;
    private Boolean disconnectedNodeAcknowledged;

    /**
     * The SnippetDTO that is being serialized.
     *
     * @return The SnippetDTO object
     */
    @ApiModelProperty("The snippet.")
    public SnippetDTO getSnippet() {
        return snippet;
    }

    public void setSnippet(SnippetDTO snippet) {
        this.snippet = snippet;
    }

    @ApiModelProperty("The position.")
    public PositionDTO getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(PositionDTO positionOffset) {
        this.positionOffset = positionOffset;
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
