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

import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.nifi.authorization.AuthorizeControllerServiceReference;
import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.ComponentAuthorizable;
import org.apache.nifi.authorization.ProcessGroupAuthorizable;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.NiFiServiceFacade;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.util.ControllerServiceAdditionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * @author weiwei.zhan
 * RESTful endpoint for managing Recycle Bin
 */
@Component
@Path("/recycle-bin")
@Api(value = "/recycle-bin")
public class OrchsymRecycleBinResource extends AbsOrchsymResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchsymRecycleBinResource.class);

    @Autowired
    private NiFiServiceFacade serviceFacade;
    @Autowired
    private Authorizer authorizer;


    @DELETE
    @Path("/empty")
    @Consumes(org.springframework.http.MediaType.ALL_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Empty recycle bin",
            response = String.class,
            authorizations = {
                    // Controller Service
                    @Authorization(value = "Write - /controller-services/{uuid}"),
                    @Authorization(value = "Write - Parent Process Group if the Controller Service is scoped by Process Group - /process-groups/{uuid}"),
                    @Authorization(value = "Write - Controller if scoped by Controller - /controller"),
                    @Authorization(value = "Read - any referenced Controller Services - /controller-services/{uuid}"),

                    // Process Group
                    @Authorization(value = "Write - /process-groups/{uuid}"),
                    @Authorization(value = "Write - Parent Process Group of this Process Group- /process-groups/{uuid}"),
                    @Authorization(value = "Read - any referenced Controller Services by any encapsulated components in this Process Group - /controller-services/{uuid}"),
                    @Authorization(value = "Write - /{component-type}/{uuid} - For all encapsulated components in this Process Group"),

                    // Template
                    @Authorization(value = "Write - /templates/{uuid}"),
                    @Authorization(value = "Write - Parent Process Group of this template - /process-groups/{uuid}")
            }
    )
    public Response emptyRecycleBin(@Context HttpServletRequest httpServletRequest) {
        removeControllerServicesInRecycleBin();
        removeAppsInRecycleBin();
        removeTemplatesInRecycleBin();

        return generateStringOkResponse("success");
    }

    // ----------------------------------------------------
    // Controller Service
    // ----------------------------------------------------

    /**
     * Remove all Controller Service in the Recycle Bin
     */
    private void removeControllerServicesInRecycleBin() {
        final Set<ControllerServiceEntity> controllerServiceEntities = getAllControllerServiceInRecycleBin();
        for (ControllerServiceEntity controllerServiceEntity: controllerServiceEntities) {
            removeControllerService(controllerServiceEntity);
        }
    }

    /**
     * Try to delete a Controller Service in the recycle bin
     */
    private void removeControllerService(final ControllerServiceEntity controllerServiceToDelete) {
        final Revision revision = getRevision(controllerServiceToDelete.getRevision(), controllerServiceToDelete.getId());
        withWriteLock(
                serviceFacade,
                controllerServiceToDelete,
                revision,
                lookup -> {
                    final ComponentAuthorizable controllerService = lookup.getControllerService(controllerServiceToDelete.getId());

                    // ensure write permission to the controller service
                    controllerService.getAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // ensure write permission to the parent process group
                    controllerService.getAuthorizable().getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // verify any referenced services
                    AuthorizeControllerServiceReference.authorizeControllerServiceReferences(controllerService, authorizer, lookup, false);
                },
                null,
                (rv, controllerServiceEntity) -> {
                    // delete the specified controller service
                    final ControllerServiceEntity entity = serviceFacade.deleteControllerService(rv, controllerServiceEntity.getId());
                    return generateOkResponse(entity).build();
                }
        );
    }

    /**
     * Get all Controller Services in the recycle bin
     */
    private Set<ControllerServiceEntity> getAllControllerServiceInRecycleBin() {
        return serviceFacade.getControllerServices(null, true, true).stream()
                .filter(ControllerServiceAdditionUtils.CONTROLLER_SERVICE_DELETED)
                .collect(Collectors.toSet());
    }

    // ----------------------------------------------------
    // Application
    // ----------------------------------------------------

    /**
     * Remove all App in the Recycle Bin
     */
    private void removeAppsInRecycleBin() {
        final Set<ProcessGroupEntity> apps = getAllAppsInRecycleBin();
        for (ProcessGroupEntity app: apps) {
            removeApp(app);
        }
    }

    /**
     * Try to delete an application
     */
    private void removeApp(final ProcessGroupEntity processGroupToDelete) {
        final Revision revision = getRevision(processGroupToDelete.getRevision(), processGroupToDelete.getId());
        withWriteLock(
                serviceFacade,
                processGroupToDelete,
                revision,
                lookup -> {
                    final ProcessGroupAuthorizable processGroupAuthorizable = lookup.getProcessGroup(processGroupToDelete.getId());

                    // ensure write to this process group and all encapsulated components including templates and controller services. additionally, ensure
                    // read to any referenced services by encapsulated components
                    authorizeProcessGroup(processGroupAuthorizable, authorizer, lookup, RequestAction.WRITE, true, true, true, false);

                    // ensure write permission to the parent process group, if applicable... if this is the root group the
                    // request will fail later but still need to handle authorization here
                    final Authorizable parentAuthorizable = processGroupAuthorizable.getAuthorizable().getParentAuthorizable();
                    if (parentAuthorizable != null) {
                        parentAuthorizable.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                    }
                },
                () -> serviceFacade.verifyDeleteProcessGroup(processGroupToDelete.getId(), true),
                (rv, processGroupEntity) -> {
                    // delete the process group
                    final ProcessGroupEntity entity = serviceFacade.deleteProcessGroup(rv, processGroupEntity.getId());

                    // create the response
                    return generateOkResponse(entity).build();
                }
        );
    }

    /**
     * Get all application in the recycle bin
     */
    private Set<ProcessGroupEntity> getAllAppsInRecycleBin() {
        return serviceFacade.getProcessGroups(FlowController.ROOT_GROUP_ID_ALIAS).stream()
                .filter(processGroupEntity -> ProcessUtil.getAdditionBooleanValue(processGroupEntity.getComponent().getAdditions(), AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT))
                .collect(Collectors.toSet());
    }


    // ----------------------------------------------------
    // Template
    // ----------------------------------------------------

    /**
     * Remove all templates in the Recycle Bin
     */
    private void removeTemplatesInRecycleBin() {
        final Set<TemplateEntity> templates = getAllTemplatesInRecycleBin();
        for (TemplateEntity template: templates) {
            removeTemplate(template);
        }
    }

    /**
     * Try to delete a template
     */
    private void removeTemplate(final TemplateEntity templateToDelete) {
        withWriteLock(
                serviceFacade,
                templateToDelete,
                lookup -> {
                    final Authorizable template = lookup.getTemplate(templateToDelete.getId());

                    // ensure write permission to the template
                    template.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // ensure write permission to the parent process group
                    template.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                },
                null,
                (templateEntity) -> {
                    // delete the specified template
                    serviceFacade.deleteTemplate(templateEntity.getId());

                    // build the response entity
                    final TemplateEntity entity = new TemplateEntity();

                    return generateOkResponse(entity).build();
                }
        );
    }

    /**
     * Get all templates in the recycle bin
     */
    private Set<TemplateEntity> getAllTemplatesInRecycleBin() {
        return serviceFacade.getTemplates().stream()
                .filter(templateEntity -> ProcessUtil.getAdditionBooleanValue(templateEntity.getTemplate().getAdditions(), AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT))
                .collect(Collectors.toSet());
    }

}
