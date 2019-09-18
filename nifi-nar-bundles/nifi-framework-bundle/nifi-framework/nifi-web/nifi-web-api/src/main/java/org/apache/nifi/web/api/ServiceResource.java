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
package org.apache.nifi.web.api;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.AuthorizeControllerServiceReference;
import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.ComponentAuthorizable;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.bundle.Bundle;
import org.apache.nifi.bundle.BundleCoordinate;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.nar.ExtensionManager;
import org.apache.nifi.web.NiFiServiceFacade;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.dto.BundleDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceSimpleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import net.minidev.json.JSONObject;

/**
 * RESTful endpoint for managing a Controller Service.
 */
@Component
@Path("/service")
@Api(
        value = "/service",
        description = "Endpoint for managing a Controller Service."
)
public class ServiceResource extends ApplicationResource {

    @Autowired
    private NiFiServiceFacade serviceFacade;
    @Autowired
    private Authorizer authorizer;
    @Autowired
    private ControllerServiceResource controllerServiceResource;

    /**
     * Updates the specified a new Controller Service.
     *
     * @param httpServletRequest             request
     * @param groupId                        The id of the controller service to update.
     * @param requestControllerServiceEntity A controllerServiceEntity.
     * @return A controllerServiceEntity.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(
            value = "Creates a new controller service",
            response = ControllerServiceEntity.class,
            authorizations = {
                    @Authorization(value = "Write - /services/{uuid}"),
                    @Authorization(value = "Read - any referenced Controller Services if this request changes the reference - /controller-services/{uuid}")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
                    @ApiResponse(code = 401, message = "Client could not be authenticated."),
                    @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
                    @ApiResponse(code = 404, message = "The specified resource could not be found."),
                    @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.")
            }
    )
    public Response updateControllerService(
            @Context HttpServletRequest httpServletRequest,
            @ApiParam(
                    value = "The process group id.",
                    required = true
            )
            @PathParam("id") final String groupId,
            @ApiParam(
                    value = "The controller service configuration details.",
                    required = true
            ) final ControllerServiceSimpleEntity requestControllerServiceEntity) {

        if (requestControllerServiceEntity == null || requestControllerServiceEntity.getService() == null) {
            throw new IllegalArgumentException("Controller service details must be specified.");
        }

        final ControllerServiceDTO requestControllerService = requestControllerServiceEntity.getComponent();
        if (requestControllerService.getId() != null) {
            throw new IllegalArgumentException("Controller service ID cannot be specified.");
        }

        if (StringUtils.isBlank(requestControllerService.getType())) {
            throw new IllegalArgumentException("The type of controller service to create must be specified.");
        }

        List<Bundle> bundles = ExtensionManager.getBundles(requestControllerService.getType());
        if (bundles.isEmpty()) {
            throw new IllegalArgumentException("Query Bundle Error");
        }

        if (requestControllerService.getParentGroupId() != null && !groupId.equals(requestControllerService.getParentGroupId())) {
            throw new IllegalArgumentException(String.format("If specified, the parent process group id %s must be the same as specified in the URI %s",
                    requestControllerService.getParentGroupId(), groupId));
        }
        requestControllerService.setParentGroupId(groupId);

        RevisionDTO revisionDTO = requestControllerServiceEntity.getRevision();
        if (revisionDTO == null) {
            revisionDTO = new RevisionDTO();
            requestControllerServiceEntity.setRevision(revisionDTO);
        }
        if (StringUtils.isBlank(revisionDTO.getClientId())) {
            revisionDTO.setClientId(generateUuid());
        }
        revisionDTO.setVersion(0L);

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, requestControllerServiceEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestControllerServiceEntity.isDisconnectedNodeAcknowledged());
        }

        String state = null;
        if (!StringUtils.isBlank(requestControllerService.getState())) {
            state = requestControllerService.getState();
            requestControllerService.setState(null);
        }

        // 自动填充 Bundle
        BundleCoordinate bundleCoordinate = bundles.get(0).getBundleDetails().getCoordinate();
        requestControllerService.setBundle(new BundleDTO(bundleCoordinate.getGroup(), bundleCoordinate.getId(), bundleCoordinate.getVersion()));
        requestControllerServiceEntity.setComponent(requestControllerService);
        Response responseGroup = withWriteLock(
                serviceFacade,
                requestControllerServiceEntity,
                lookup -> {
                    final NiFiUser user = NiFiUserUtils.getNiFiUser();

                    final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, user);

                    ComponentAuthorizable authorizable = null;
                    try {
                        authorizable = lookup.getConfigurableComponent(requestControllerService.getType(), requestControllerService.getBundle());

                        if (authorizable.isRestricted()) {
                            authorizeRestrictions(authorizer, authorizable);
                        }

                        if (requestControllerService.getProperties() != null) {
                            AuthorizeControllerServiceReference.authorizeControllerServiceReferences(requestControllerService.getProperties(), authorizable, authorizer, lookup);
                        }
                    } finally {
                        if (authorizable != null) {
                            authorizable.cleanUpResources();
                        }
                    }
                },
                () -> serviceFacade.verifyCreateControllerService(requestControllerService),
                controllerServiceEntity -> {
                    final ControllerServiceDTO controllerService = controllerServiceEntity.getComponent();

                    // set the processor id as appropriate
                    controllerService.setId(generateUuid());

                    // create the controller service and generate the json
                    final Revision revision = getRevision(controllerServiceEntity, controllerService.getId());
                    final ControllerServiceEntity entity = serviceFacade.createControllerService(revision, groupId, controllerService);
                    controllerServiceResource.populateRemainingControllerServiceEntityContent(entity);

                    // build the response
                    return generateCreatedResponse(URI.create(entity.getUri()), entity).build();
                }
        );

        if (!Response.Status.CREATED.equals(responseGroup.getStatusInfo())) {
            JSONObject result = new JSONObject();
            result.put("code", responseGroup.getStatus());
            result.put("messages", "Create Group Error.");
            return Response.ok(result.toJSONString()).build();
        }

        ControllerServiceEntity controllerServiceEntityResult = (ControllerServiceEntity) responseGroup.getEntity();
        ControllerServiceDTO component = controllerServiceEntityResult.getComponent();
        JSONObject result = new JSONObject();
        result.put("messages", "Create Controller Service Success.");
        result.put("groupId", groupId);
        result.put("id", controllerServiceEntityResult.getId());
        result.put("name", component.getName());
        result.put("description", component.getComments());
        result.put("validationStatus", component.getValidationStatus());
        result.put("validationErrors", component.getValidationErrors());

        // 启动服务
        if (ControllerServiceDTO.VALID.equalsIgnoreCase(component.getValidationStatus()) &&
                ControllerServiceState.ENABLED.name().equalsIgnoreCase(state)) {
            final Revision requestRevision = getRevision(requestControllerServiceEntity, requestControllerService.getId());
            ControllerServiceSimpleEntity requestControllerServiceEntityEnable = new ControllerServiceSimpleEntity();
            revisionDTO.setVersion(1L);
            requestControllerServiceEntityEnable.setRevision(revisionDTO);
            ControllerServiceDTO serviceDTO = new ControllerServiceDTO();
            serviceDTO.setId(component.getId());
            serviceDTO.setState(state);
            requestControllerServiceEntityEnable.setComponent(serviceDTO);
            Response responseEnable = withWriteLock(
                    serviceFacade,
                    requestControllerServiceEntityEnable,
                    requestRevision,
                    lookup -> {
                        // authorize the service
                        final ComponentAuthorizable authorizable = lookup.getControllerService(requestControllerService.getId());
                        authorizable.getAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                        // authorize any referenced services
                        AuthorizeControllerServiceReference.authorizeControllerServiceReferences(requestControllerService.getProperties(), authorizable, authorizer, lookup);
                    },
                    () -> serviceFacade.verifyUpdateControllerService(requestControllerService),
                    (revision, controllerServiceEntity) -> {
                        final ControllerServiceDTO controllerService = controllerServiceEntity.getComponent();

                        // update the controller service
                        final ControllerServiceEntity entity = serviceFacade.updateControllerService(revision, controllerService);
                        controllerServiceResource.populateRemainingControllerServiceEntityContent(entity);

                        return generateOkResponse(entity).build();
                    }
            );
            component = ((ControllerServiceEntity) responseEnable.getEntity()).getComponent();
            result.put("code", responseEnable.getStatus());
        } else {
            result.put("code", Response.Status.OK);
        }
        result.put("state", component.getState());
        return Response.ok(result.toJSONString()).build();
    }
}
