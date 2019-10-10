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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.attribute.expression.language.PreparedQuery;
import org.apache.nifi.attribute.expression.language.Query;
import org.apache.nifi.attribute.expression.language.SensitivePropertyValue;
import org.apache.nifi.attribute.expression.language.StandardPropertyValue;
import org.apache.nifi.authorization.AuthorizeControllerServiceReference;
import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.ComponentAuthorizable;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.bundle.Bundle;
import org.apache.nifi.bundle.BundleCoordinate;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.nar.ExtensionManager;
import org.apache.nifi.web.NiFiServiceFacade;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.dto.BundleDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DbcpControllerServiceDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.VariableRegistryDTO;
import org.apache.nifi.web.api.dto.dbcp.DbcpMetadataDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;
import org.apache.nifi.web.api.entity.ControllerServiceSimpleEntity;
import org.apache.nifi.web.api.entity.ControllerServicesBatchOperationEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.DbcpControllerServiceEntity;
import org.apache.nifi.web.api.entity.DbcpControllerServicesEntity;
import org.apache.nifi.web.api.entity.VariableEntity;
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
@Api(value = "/service", description = "Endpoint for managing a Controller Service.")
public class ServiceResource extends AbsOrchsymResource {
    private static final String DBCP_CLASS = "org.apache.nifi.dbcp.DBCPConnectionPool";

    @Autowired
    private NiFiServiceFacade serviceFacade;
    @Autowired
    private Authorizer authorizer;
    @Autowired
    private ControllerServiceResource controllerServiceResource;

    /**
     * Updates the specified a new Controller Service.
     *
     * @param httpServletRequest
     *            request
     * @param groupId
     *            The id of the controller service to update.
     * @param requestControllerServiceEntity
     *            A controllerServiceEntity.
     * @return A controllerServiceEntity.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Creates a new controller service", response = ControllerServiceEntity.class, authorizations = { @Authorization(value = "Write - /services/{uuid}"),
            @Authorization(value = "Read - any referenced Controller Services if this request changes the reference - /controller-services/{uuid}") })
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response updateControllerService(@Context HttpServletRequest httpServletRequest, @ApiParam(value = "The process group id.", required = true) @PathParam("id") final String groupId,
            @ApiParam(value = "The controller service configuration details.", required = true) final ControllerServiceSimpleEntity requestControllerServiceEntity) {

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
            throw new IllegalArgumentException(
                    String.format("If specified, the parent process group id %s must be the same as specified in the URI %s", requestControllerService.getParentGroupId(), groupId));
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
        Response responseGroup = withWriteLock(serviceFacade, requestControllerServiceEntity, lookup -> {
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
        }, () -> serviceFacade.verifyCreateControllerService(requestControllerService), controllerServiceEntity -> {
            final ControllerServiceDTO controllerService = controllerServiceEntity.getComponent();

            // set the processor id as appropriate
            controllerService.setId(generateUuid());

            // create the controller service and generate the json
            final Revision revision = getRevision(controllerServiceEntity, controllerService.getId());
            final ControllerServiceEntity entity = serviceFacade.createControllerService(revision, groupId, controllerService);
            controllerServiceResource.populateRemainingControllerServiceEntityContent(entity);

            // update process group variables
            final Set<VariableEntity> variableEntitySet = requestControllerServiceEntity.variablesToVariableEntities();
            if (variableEntitySet != null) {
                VariableRegistryDTO variableRegistryDTO = new VariableRegistryDTO();
                variableRegistryDTO.setProcessGroupId(groupId);
                variableRegistryDTO.setVariables(variableEntitySet);
                serviceFacade.updateVariableRegistry(revision, variableRegistryDTO);
            }
            // build the response
            return generateCreatedResponse(URI.create(entity.getUri()), entity).build();
        });

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
        if (ControllerServiceDTO.VALID.equalsIgnoreCase(component.getValidationStatus()) && ControllerServiceState.ENABLED.name().equalsIgnoreCase(state)) {
            final Revision requestRevision = getRevision(requestControllerServiceEntity, requestControllerService.getId());
            ControllerServiceSimpleEntity requestControllerServiceEntityEnable = new ControllerServiceSimpleEntity();
            revisionDTO.setVersion(1L);
            requestControllerServiceEntityEnable.setRevision(revisionDTO);
            ControllerServiceDTO serviceDTO = new ControllerServiceDTO();
            serviceDTO.setId(component.getId());
            serviceDTO.setState(state);
            requestControllerServiceEntityEnable.setComponent(serviceDTO);
            Response responseEnable = withWriteLock(serviceFacade, requestControllerServiceEntityEnable, requestRevision, lookup -> {
                // authorize the service
                final ComponentAuthorizable authorizable = lookup.getControllerService(requestControllerService.getId());
                authorizable.getAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                // authorize any referenced services
                AuthorizeControllerServiceReference.authorizeControllerServiceReferences(requestControllerService.getProperties(), authorizable, authorizer, lookup);
            }, () -> serviceFacade.verifyUpdateControllerService(requestControllerService), (revision, controllerServiceEntity) -> {
                final ControllerServiceDTO controllerService = controllerServiceEntity.getComponent();

                // update the controller service
                final ControllerServiceEntity entity = serviceFacade.updateControllerService(revision, controllerService);
                controllerServiceResource.populateRemainingControllerServiceEntityContent(entity);

                return generateOkResponse(entity).build();
            });
            component = ((ControllerServiceEntity) responseEnable.getEntity()).getComponent();
            result.put("code", responseEnable.getStatus());
        } else {
            result.put("code", Response.Status.OK);
        }
        result.put("state", component.getState());
        return Response.ok(result.toJSONString()).build();
    }

    /**
     * Try to enable/disable/delete all the controller services in the specified process group.
     *
     * @param httpServletRequest
     *            request
     * @param groupId
     *            The process group id.
     * @param requestOperationEntity
     *            A controllerServicesBatchOperationEntity
     * @return A controllerServicesEntity.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/operation")
    @ApiOperation(value = "batch enable/disable/delete controller services in a process group", response = ControllerServicesEntity.class, authorizations = {
            @Authorization(value = "Write - /controller-services/{uuid}"), @Authorization(value = "Write - Parent Process Group if scoped by Process Group - /process-groups/{uuid}"),
            @Authorization(value = "Write - Controller if scoped by Controller - /controller"),
            @Authorization(value = "Read - any referenced Controller Services if this request changes the reference - /controller-services/{uuid}") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 404, message = "The specified resource could not be found."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
    public Response updateControllerServices(@Context HttpServletRequest httpServletRequest, @ApiParam(value = "The process group id.", required = true) @PathParam("id") final String groupId,
            @ApiParam(value = "The controller service configuration details.", required = true) final ControllerServicesBatchOperationEntity requestOperationEntity) {

        if (requestOperationEntity == null) {
            throw new IllegalArgumentException("Controller service details must be specified.");
        }

        ControllerServicesBatchOperationEntity.ControllerServiceBatchOperation controllerServiceBatchOperation = null;
        try {
            controllerServiceBatchOperation = ControllerServicesBatchOperationEntity.ControllerServiceBatchOperation.valueOf(requestOperationEntity.getOperation());
        } catch (final IllegalArgumentException iae) {
            // ignore
        }
        if (controllerServiceBatchOperation == null) {
            throw new IllegalArgumentException("Must specify the operation. Allowable values are: ENABLE, DISABLE, DELETE");
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, requestOperationEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestOperationEntity.isDisconnectedNodeAcknowledged());
        }

        ControllerServicesBatchOperationEntity.ControllerServiceBatchOperation operation = controllerServiceBatchOperation;
        return withWriteLock(serviceFacade, requestOperationEntity, lookup -> {
            final NiFiUser user = NiFiUserUtils.getNiFiUser();

            // ensure write on the group
            final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.WRITE, user);
        }, null, operationEntity -> {
            ControllerServicesEntity responseEntity = new ControllerServicesEntity();
            responseEntity.setCurrentTime(new Date());

            final boolean includeDescendantGroups = operationEntity.isIncludeDescendantGroups();
            // get the controller services
            final Set<ControllerServiceEntity> controllerServiceEntities = serviceFacade.getControllerServices(groupId, false, includeDescendantGroups);

            switch (operation) {
            case ENABLE:
                enableControllerServices(controllerServiceEntities);
                responseEntity.setControllerServices(serviceFacade.getControllerServices(groupId, false, includeDescendantGroups));
                break;
            case DISABLE:
                disableControllerServices(controllerServiceEntities);
                responseEntity.setControllerServices(serviceFacade.getControllerServices(groupId, false, includeDescendantGroups));
                break;
            case DELETE:
                disableControllerServices(controllerServiceEntities);
                final Set<ControllerServiceEntity> disabledControllerServiceEntities = serviceFacade.getControllerServices(groupId, false, includeDescendantGroups);
                responseEntity.setControllerServices(deleteControllerServices(disabledControllerServiceEntities));
                break;
            default:
                break;
            }

            // generate the response
            return generateCreatedResponse(getAbsolutePath(), responseEntity).build();
        });
    }

    // Try to enable a bulk of controller services.
    private void enableControllerServices(Set<ControllerServiceEntity> servicesToEnable) {
        for (ControllerServiceEntity serviceEntity : servicesToEnable) {
            final ControllerServiceDTO serviceDTO = new ControllerServiceDTO();
            serviceDTO.setId(serviceEntity.getId());
            serviceDTO.setState(ControllerServiceState.ENABLED.name());

            final Revision revision = getRevision(serviceEntity.getRevision(), serviceDTO.getId());
            final ControllerServiceEntity controllerServiceEntity = serviceFacade.updateControllerService(revision, serviceDTO);
            controllerServiceResource.populateRemainingControllerServiceEntityContent(controllerServiceEntity);
        }
    }

    // Try to disable a bulk of controller services.
    private void disableControllerServices(Set<ControllerServiceEntity> servicesToDisable) {
        for (ControllerServiceEntity serviceEntity : servicesToDisable) {
            // stop the controller service references
            final Set<ControllerServiceReferencingComponentEntity> referencingComponentEntities = serviceFacade.getControllerServiceReferencingComponents(serviceEntity.getId())
                    .getControllerServiceReferencingComponents();
            final Map<String, Revision> referencingRevisions = referencingComponentEntities.stream().collect(Collectors.toMap(ControllerServiceReferencingComponentEntity::getId, entity -> {
                final RevisionDTO rev = entity.getRevision();
                return new Revision(rev.getVersion(), rev.getClientId(), entity.getId());
            }));
            serviceFacade.updateControllerServiceReferencingComponents(referencingRevisions, serviceEntity.getId(), ScheduledState.STOPPED, null);
            // disable the controller service references
            serviceFacade.updateControllerServiceReferencingComponents(new HashMap<>(), serviceEntity.getId(), ScheduledState.DISABLED, ControllerServiceState.DISABLED);
            // disable the controller service

            final ControllerServiceDTO serviceDTO = new ControllerServiceDTO();
            serviceDTO.setId(serviceEntity.getId());
            serviceDTO.setState(ControllerServiceState.DISABLED.name());

            final Revision revision = getRevision(serviceEntity.getRevision(), serviceDTO.getId());
            final ControllerServiceEntity controllerServiceEntity = serviceFacade.updateControllerService(revision, serviceDTO);
            controllerServiceResource.populateRemainingControllerServiceEntityContent(controllerServiceEntity);

        }
    }

    // try to delete a bulk of controller services
    private Set<ControllerServiceEntity> deleteControllerServices(Set<ControllerServiceEntity> servicesToDelete) {
        Set<ControllerServiceEntity> deletedControllerServices = new HashSet<>();
        for (ControllerServiceEntity serviceEntity : servicesToDelete) {
            final Revision revision = getRevision(serviceEntity.getRevision(), serviceEntity.getId());
            deletedControllerServices.add(serviceFacade.deleteControllerService(revision, serviceEntity.getId()));
        }
        return deletedControllerServices;
    }

    /**
     * Retrieves the specified controller service.
     *
     * @return A controllerServicesEntity.
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/databases")
    @ApiOperation(value = "Gets DBCP services", response = ControllerServicesEntity.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getDbcpControllerServices() {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        // authorize access
        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable rootProcessGroup = lookup.getProcessGroup(FlowController.ROOT_GROUP_ID_ALIAS).getAuthorizable();
            rootProcessGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        final Set<ControllerServiceEntity> controllerServiceEntitySet = serviceFacade.getControllerServices(FlowController.ROOT_GROUP_ID_ALIAS, true, true);
        DbcpControllerServicesEntity entity = new DbcpControllerServicesEntity();
        final Set<DbcpControllerServiceDTO> dbcpControllerServiceDTOSet = controllerServiceEntitySet.stream()
                .filter(controllerServiceEntity -> controllerServiceEntity.getComponent().getType().equals(DBCP_CLASS)).map(DbcpControllerServiceDTO::new).collect(Collectors.toSet());
        Set<DbcpControllerServiceEntity> dbcpControllerServicesEntitySet = new HashSet<>();
        for (DbcpControllerServiceDTO dbcpControllerServiceDTO : dbcpControllerServiceDTOSet) {
            DbcpControllerServiceEntity dbcpControllerServiceEntity = new DbcpControllerServiceEntity();
            dbcpControllerServiceEntity.setDbcpControllerService(dbcpControllerServiceDTO);
            dbcpControllerServicesEntitySet.add(dbcpControllerServiceEntity);
        }
        entity.setDbcpControllerServices(dbcpControllerServicesEntitySet);
        entity.setCurrentTime(new Date(System.currentTimeMillis()));
        return generateOkResponse(entity).build();
    }

    private static final String DATABASE_CONNECTION_URL = "Database Connection URL";
    private static final String DATABASE_DRIVER_CLASS_NAME = "Database Driver Class Name";
    private static final String DATABASE_DRIVER_LOCATION = "database-driver-locations";
    private static final String DATABASE_USER = "Database User";
    private static final String DATABASE_PASSWORD = "Password";

    /**
     * Retrieves the specified controller service.
     *
     * @return A controllerServicesEntity.
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dbcp/{id}/metadata")
    @ApiOperation(value = "Gets metadata of DBCP services", response = ControllerServicesEntity.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getDbMetadata(@ApiParam(value = "The controller service id.", required = true) @PathParam("id") final String id) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        // authorize access
        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable controllerService = lookup.getControllerService(id).getAuthorizable();
            controllerService.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        final DbcpMetadataDTO databaseMeta = getDatabaseMeta(id);
        if (null == databaseMeta) {
            throw new IllegalArgumentException("The controller service [" + id + "] is not a database connection.");
        }
        return generateOkResponse(databaseMeta).build();

    }

    private DbcpMetadataDTO getDatabaseMeta(final String serviceIdentifier) {
        final ControllerServiceNode controllerServiceNode = flowController.getControllerServiceNode(serviceIdentifier);
        if (!DBCP_CLASS.equals(controllerServiceNode.getCanonicalClassName())) {
            return null;
        }
        final String drv = getProperty(controllerServiceNode, DATABASE_DRIVER_CLASS_NAME);
        final String user = getProperty(controllerServiceNode, DATABASE_USER);
        final String passw = getProperty(controllerServiceNode, DATABASE_PASSWORD);
        final String urlString = getProperty(controllerServiceNode, DATABASE_DRIVER_LOCATION);
        final String dburl = getProperty(controllerServiceNode, DATABASE_CONNECTION_URL);

        DbcpMetadataDTO databaseMeta = new DbcpMetadataDTO();
        databaseMeta.setId(serviceIdentifier);

        databaseMeta.setDriverClassName(drv);
        databaseMeta.setConnectionUrl(dburl);
        databaseMeta.setDbUser(user);
        databaseMeta.setDbPassword(passw);

        Set<String> modules = new LinkedHashSet<>();
        if (urlString != null) {
            modules = Arrays.stream(urlString.split(",")).filter(path -> StringUtils.isNoneBlank(path)).map(String::trim).collect(Collectors.toSet());
        }
        databaseMeta.setDriverLocations(modules.toArray(new String[0]));

        final Map<String, String> connectionProperties = controllerServiceNode.getPropertyDescriptors().stream().filter(desc -> desc.isDynamic())
                .collect(Collectors.toMap(PropertyDescriptor::getName, desc -> getProperty(controllerServiceNode, desc.getName())));
        databaseMeta.setConnectionProperties(connectionProperties);

        databaseMeta.setState(controllerServiceNode.getState().name());
        return databaseMeta;
    }

    private String getProperty(final ControllerServiceNode controllerServiceNode, String propertyName) {
        final ControllerService controllerService = controllerServiceNode.getProxiedControllerService();
        final PropertyDescriptor descriptor = controllerService.getPropertyDescriptor(propertyName);
        if (descriptor == null) {
            throw new IllegalArgumentException("Property [" + propertyName + "] does not exist!");
        }

        final String setPropertyValue = controllerServiceNode.getProperty(descriptor);
        final String propValue = (setPropertyValue == null) ? descriptor.getDefaultValue() : setPropertyValue;

        Map<PropertyDescriptor, PreparedQuery> preparedQueries = new HashMap<>();
        for (final Map.Entry<PropertyDescriptor, String> entry : controllerServiceNode.getProperties().entrySet()) {
            final PropertyDescriptor desc = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                value = desc.getDefaultValue();
            }

            if (value != null) {
                final PreparedQuery pq = Query.prepare(value);
                preparedQueries.put(desc, pq);
            }
        }
        StandardPropertyValue propertyValue = null;
        if (descriptor.isSensitive()) {
            propertyValue = new SensitivePropertyValue(propValue, flowController, preparedQueries.get(descriptor), controllerServiceNode.getVariableRegistry());
        } else {
            propertyValue = new StandardPropertyValue(propValue, flowController, preparedQueries.get(descriptor), controllerServiceNode.getVariableRegistry());
        }
        return propertyValue.evaluateAttributeExpressions().getValue(true);
    }
}
