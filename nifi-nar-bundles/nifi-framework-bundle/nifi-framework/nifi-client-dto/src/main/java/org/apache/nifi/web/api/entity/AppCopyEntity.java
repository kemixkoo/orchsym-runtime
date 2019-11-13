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
import org.apache.nifi.web.api.dto.AppCopyDTO;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author weiwei.zhan
 */
@XmlRootElement(name = "appCopyEntity")
public class AppCopyEntity extends Entity{
    private AppCopyDTO appCopy;
    private Boolean disconnectedNodeAcknowledged;

    /**
     * The AppCopyDTO that is being serialized
     * @return  The AppCopyDTO object
     */
    public AppCopyDTO getAppCopy() {
        return appCopy;
    }

    public void setAppCopy(AppCopyDTO appCopy) {
        this.appCopy = appCopy;
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
