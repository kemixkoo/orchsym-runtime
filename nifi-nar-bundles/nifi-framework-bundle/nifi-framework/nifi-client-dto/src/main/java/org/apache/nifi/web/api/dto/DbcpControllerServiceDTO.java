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

import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author weiwei.zhan
 */
@XmlRootElement(name = "dbcpControllerService")
public class DbcpControllerServiceDTO {
    private String id;
    private String parentGroupId;
    private String name;
    private String type;
    private String comments;
    private String state;
    private Map<String, String> properties;
    private Set<DbcpServiceComponentReferenceDTO> dbcpServiceComponentReferences;

    public DbcpControllerServiceDTO (ControllerServiceEntity controllerServiceEntity) {
        final ControllerServiceDTO controllerServiceDTO = controllerServiceEntity.getComponent();
        this.id = controllerServiceDTO.getId();
        this.parentGroupId = controllerServiceDTO.getParentGroupId();
        this.name = controllerServiceDTO.getName();
        this.type = controllerServiceDTO.getType();
        this.comments = controllerServiceDTO.getComments();
        this.state = controllerServiceDTO.getState();
        this.properties = controllerServiceDTO.getProperties();

        final Set<ControllerServiceReferencingComponentEntity> controllerServiceReferencingComponentEntitySet = controllerServiceDTO.getReferencingComponents();
        Set<DbcpServiceComponentReferenceDTO> dbcpServiceComponentReferenceDTOSet = new HashSet<>();
        for(ControllerServiceReferencingComponentEntity entity: controllerServiceReferencingComponentEntitySet) {
            dbcpServiceComponentReferenceDTOSet.add(new DbcpServiceComponentReferenceDTO(entity));
        }

        this.dbcpServiceComponentReferences = dbcpServiceComponentReferenceDTOSet;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Set<DbcpServiceComponentReferenceDTO> getDbcpServiceComponentReferences() {
        return dbcpServiceComponentReferences;
    }

    public void setDbcpServiceComponentReferences(Set<DbcpServiceComponentReferenceDTO> dbcpServiceComponentReferences) {
        this.dbcpServiceComponentReferences = dbcpServiceComponentReferences;
    }
}

class DbcpServiceComponentReferenceDTO {
    private String id;
    private String groupId;
    private String name;
    private String type;
    private String state;

    DbcpServiceComponentReferenceDTO(ControllerServiceReferencingComponentEntity controllerServiceReferencingComponentEntity) {
        final ControllerServiceReferencingComponentDTO controllerServiceReferencingComponentDTO = controllerServiceReferencingComponentEntity.getComponent();
        this.id = controllerServiceReferencingComponentDTO.getId();
        this.groupId = controllerServiceReferencingComponentDTO.getGroupId();
        this.name = controllerServiceReferencingComponentDTO.getName();
        this.type = controllerServiceReferencingComponentDTO.getType();
        this.state = controllerServiceReferencingComponentDTO.getState();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
