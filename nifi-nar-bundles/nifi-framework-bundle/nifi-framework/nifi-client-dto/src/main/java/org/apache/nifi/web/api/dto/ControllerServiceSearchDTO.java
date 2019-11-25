/*
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
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;

import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author weiwei.zhan
 * Class used to providing details in Controller Service search result
 */
@XmlType(name = "controllerService")
public class ControllerServiceSearchDTO extends ComponentDTO {
    public static final String VALID = "VALID";
    public static final String INVALID = "INVALID";
    public static final String VALIDATING = "VALIDATING";

    private String name;
    private String type;
    private String uri;
    private String comments;
    private String scope;
    private String state;
    private Boolean persistsState;
    private Boolean restricted;
    private Boolean deprecated;
    private Boolean isExtensionMissing;
    private Boolean multipleVersionsAvailable;
    private String annotationData;
    private Map<String, Set<String>> referencingComponents;
    private Collection<String> validationErrors;
    private String validationStatus;
    private Map<String, String> additions;
    private ApplicationInfoDTO info;

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
     * @return the controller service type
     */
    @ApiModelProperty(
            value = "The type of the controller service."
    )
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The uri for linking to this component in this NiFi.
     *
     * @return The uri
     */
    @ApiModelProperty(
            value = "The URI for futures requests to the component."
    )
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    /**
     * @return  the Controller Service scope
     */
    @ApiModelProperty(
            value = "The scope of the Controller Service."
    )
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return whether this controller service persists state
     */
    @ApiModelProperty(
        value = "Whether the controller service persists state."
    )
    public Boolean getPersistsState() {
        return persistsState;
    }

    public void setPersistsState(Boolean persistsState) {
        this.persistsState = persistsState;
    }

    /**
     * @return whether this controller service requires elevated privileges
     */
    @ApiModelProperty(
            value = "Whether the controller service requires elevated privileges."
    )
    public Boolean getRestricted() {
        return restricted;
    }

    public void setRestricted(Boolean restricted) {
        this.restricted = restricted;
    }

    /**
     * @return Whether the controller service has been deprecated.
     */
    @ApiModelProperty(
            value = "Whether the ontroller service has been deprecated."
    )
    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated= deprecated;
    }

    /**
     * @return whether the underlying extension is missing
     */
    @ApiModelProperty(
            value = "Whether the underlying extension is missing."
    )
    public Boolean getExtensionMissing() {
        return isExtensionMissing;
    }

    public void setExtensionMissing(Boolean extensionMissing) {
        isExtensionMissing = extensionMissing;
    }

    /**
     * @return whether this controller service has multiple versions available
     */
    @ApiModelProperty(
            value = "Whether the controller service has multiple versions available."
    )
    public Boolean getMultipleVersionsAvailable() {
        return multipleVersionsAvailable;
    }

    public void setMultipleVersionsAvailable(Boolean multipleVersionsAvailable) {
        this.multipleVersionsAvailable = multipleVersionsAvailable;
    }

    /**
     * @return The state of this controller service. Possible values are ENABLED, ENABLING, DISABLED, DISABLING
     */
    @ApiModelProperty(
            value = "The state of the controller service.",
            allowableValues = "ENABLED, ENABLING, DISABLED, DISABLING"
    )
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return annotation data for this controller service
     */
    @ApiModelProperty(
            value = "The annotation for the controller service. This is how the custom UI relays configuration to the controller service."
    )
    public String getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(String annotationData) {
        this.annotationData = annotationData;
    }

    /**
     * @return all components referencing this controller service
     */
    @ApiModelProperty(
            value = "All components referencing this controller service."
    )
    public Map<String, Set<String>> getReferencingComponents() {
        return referencingComponents;
    }

    public void setReferencingComponents(Map<String, Set<String>> referencingComponents) {
        this.referencingComponents = referencingComponents;
    }

    /**
     * Gets the validation errors from this controller service. These validation errors represent the problems with the controller service that must be resolved before it can be enabled.
     *
     * @return The validation errors
     */
    @ApiModelProperty(
            value = "The validation errors from the controller service. These validation errors represent the problems with the controller service that must be resolved before it can be enabled."
    )
    public Collection<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Collection<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @ApiModelProperty(value = "Indicates whether the Processor is valid, invalid, or still in the process of validating (i.e., it is unknown whether or not the Processor is valid)",
        readOnly = true,
        allowableValues = VALID + ", " + INVALID + ", " + VALIDATING)
    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Map<String, String> getAdditions() {
        return additions;
    }

    public void setAdditions(Map<String, String> additions) {
        this.additions = additions;
    }

    public ApplicationInfoDTO getInfo() {
        return info;
    }

    public void setInfo(ApplicationInfoDTO info) {
        this.info = info;
    }

    @Override
    public int hashCode() {
        final String id = getId();
        return 37 + 3 * ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (obj.getClass() != ControllerServiceSearchDTO.class) {
            return false;
        }

        final ControllerServiceSearchDTO other = (ControllerServiceSearchDTO) obj;
        if (getId() == null || other.getId() == null) {
            return false;
        }

        return getId().equals(other.getId());
    }
}
