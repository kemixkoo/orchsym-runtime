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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.connectable.Funnel;
import org.apache.nifi.connectable.Port;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.Snippet;
import org.apache.nifi.controller.label.Label;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.apache.nifi.web.revision.RevisionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * @author liuxun
 * @apiNote 处理app的相关功能
 */
@Component
@Path("/group")
@Api(value = "/group", description = "group API")
public class OrchsymGroupResource extends AbsOrchsymResource {

    @Autowired
    private RevisionManager revisionManager;

    Object getComponentById(String id) {
        // 首先查找Label
        final Label label = getLabel(id);
        if (label != null) {
            return label;
        }
        final ProcessorNode processorNode = flowController.getProcessorNode(id);
        if (processorNode != null) {
            return processorNode;
        }

        final Connection connection = flowController.getConnection(id);
        if (connection != null) {
            return connection;
        }

        final Port inputPort = flowController.getInputPort(id);
        if (inputPort != null) {
            return inputPort;
        }

        final Port outputPort = flowController.getOutputPort(id);
        if (outputPort != null) {
            return outputPort;
        }

        final Funnel funnel = flowController.getFunnel(id);
        if (funnel != null) {
            return funnel;
        }

        final ProcessGroup group = flowController.getGroup(id);
        if (group != null) {
            return group;
        }

        final ControllerService controllerService = flowController.getControllerService(id);
        if (controllerService != null) {
            return controllerService;
        }

        final ReportingTaskNode reportingTaskNode = flowController.getReportingTaskNode(id);
        if (reportingTaskNode != null) {
            return reportingTaskNode;
        }

        final Snippet snippet = flowController.getSnippetManager().getSnippet(id);
        if (snippet != null) {
            return snippet;
        }
        return null;
    }

    private Label getLabel(String id) {
        final ProcessGroup rootGroup = flowController.getGroup(flowController.getRootGroupId());
        final Label label = rootGroup.findLabel(id);
        return label;
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchFlowByGroup(//
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String value, //
            @ApiParam(value = "The group id", required = true) @PathParam("groupId") final String groupId//
    ) throws InterruptedException {

        // query the controller
        final SearchResultsDTO results = serviceFacade.searchController(value, groupId);

        // create the entity
        final SearchResultsEntity entity = new SearchResultsEntity();
        entity.setSearchResultsDTO(results);

        // generate the response
        return noCache(Response.ok(entity)).build();
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/force_delete")
    @ApiOperation(value = "delete the group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response forceDeleteGroup(@PathParam("groupId") String groupId) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }

        final ProcessGroup groupApp = flowController.getGroup(groupId);
        if (groupApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the groupId").build();
        }

        return getResponseForForceDeleteGroup(groupApp);
    }

    Response getResponseForForceDeleteGroup(ProcessGroup gp) {
        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(gp.getIdentifier());

        return withWriteLock(serviceFacade, groupEntity, lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(groupEntity.getId()).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
        }, null, (entity) -> {
            final ProcessGroup group = flowController.getGroup(entity.getId());
            // 先进行逻辑删除
            deleteGroupLogic(group);
            // 校验
            serviceFacade.verifyDeleteProcessGroup(entity.getId());
            // 物理删除
            serviceFacade.deleteProcessGroup(revisionManager.getRevision(entity.getId()), entity.getId());
            return generateOkResponse("success").build();
        });
    }

    void deleteGroupLogic(ProcessGroup group) {
        // 停止所有组件
        for (ProcessorNode processorNode : group.getProcessors()) {
            final ProcessorDTO processorDTO = new ProcessorDTO();
            processorDTO.setId(processorNode.getIdentifier());
            processorDTO.setState(ScheduleComponentsEntity.STATE_STOPPED);
            Revision revision = revisionManager.getRevision(processorNode.getIdentifier());
            serviceFacade.updateProcessor(revision, processorDTO);
        }

        // 清空队列
        for (Connection connection : group.getConnections()) {
            DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(connection.getIdentifier(), generateUuid());
            serviceFacade.deleteFlowFileDropRequest(connection.getIdentifier(), dropRequest.getId());
        }

        // 停止服务
        for (ControllerServiceNode controllerServiceNode : group.getControllerServices(false)) {
            Revision revision = revisionManager.getRevision(controllerServiceNode.getIdentifier());
            ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
            controllerServiceDTO.setId(controllerServiceNode.getIdentifier());
            controllerServiceDTO.setState(ScheduleComponentsEntity.STATE_DISABLED);
            serviceFacade.updateControllerService(revision, controllerServiceDTO);
        }

        // 迭代处理子模块
        for (ProcessGroup childGroup : group.getProcessGroups()) {
            deleteGroupLogic(childGroup);
        }
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/navigator")
    @ApiOperation(value = "Get the parents info of current app or component", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getNavigatorInfo(@PathParam("id") String id) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        final Object component = getComponentById(id);
        if (component == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, Object> infoMap = getCurrentGroup(component);
        ProcessGroup currentGroup = (ProcessGroup) infoMap.get("currentGroup");
        String name = (String) infoMap.get("name");
        if (currentGroup == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Map<String, Object> resultMap = new HashMap<>();
        Boolean isGroup = currentGroup.getIdentifier().equals(id);
        resultMap.put("id", id);
        resultMap.put("name", name);
        resultMap.put("parentGroupId", isGroup ? currentGroup.getParent().getIdentifier() : currentGroup.getIdentifier());
        resultMap.put("parentGroupName", isGroup ? currentGroup.getParent().getName() : currentGroup.getName());

        ProcessGroup tempGroup = currentGroup;
        List<Map<String, String>> groups = new ArrayList<>();
        while (!tempGroup.isRootGroup()) {
            Map<String, String> groupInfoMap = new HashMap<>();
            groupInfoMap.put("id", tempGroup.getIdentifier());
            groupInfoMap.put("name", tempGroup.getName());
            groupInfoMap.put("parentGroupId", tempGroup.getParent().getIdentifier());
            groups.add(groupInfoMap);
            tempGroup = tempGroup.getParent();
        }
        Collections.reverse(groups);
        ProcessGroup appGroup = flowController.getGroup(groups.get(0).get("id"));
        final String path = StringUtils.join(groups.stream().map(map -> map.getOrDefault("name", "")).collect(Collectors.toList()), "/");

        resultMap.put("applicationId", appGroup.getIdentifier());
        resultMap.put("applicationName", appGroup.getName());
        resultMap.put("path", path);
        resultMap.put("groups", groups);

        return Response.ok(resultMap).build();
    }

    private Map<String, Object> getCurrentGroup(Object component) {
        ProcessGroup currentGroup = null;
        String name = null;
        if (component instanceof Label) {
            Label label = (Label) component;
            currentGroup = label.getProcessGroup();
        } else if (component instanceof ProcessorNode) {
            ProcessorNode processorNode = (ProcessorNode) component;
            currentGroup = processorNode.getProcessGroup();
            name = processorNode.getName();
        } else if (component instanceof Connection) {
            Connection connection = ((Connection) component);
            currentGroup = connection.getProcessGroup();
            name = connection.getName();
        } else if (component instanceof Port) {
            Port port = (Port) component;
            currentGroup = port.getProcessGroup();
            name = port.getName();
        } else if (component instanceof Funnel) {
            Funnel funnel = (Funnel) component;
            currentGroup = funnel.getProcessGroup();
            name = funnel.getName();
        } else if (component instanceof ControllerService) {
        } else if (component instanceof ReportingTaskNode) {
            ReportingTaskNode reportingTask = (ReportingTaskNode) component;
            currentGroup = flowController.getGroup(reportingTask.getProcessGroupIdentifier());
            name = reportingTask.getName();
        } else if (component instanceof ProcessGroup) {
            currentGroup = (ProcessGroup) component;
            name = currentGroup.getName();
        } else if (component instanceof Snippet) {
            final Snippet snippet = (Snippet) component;
            currentGroup = flowController.getGroup(snippet.getParentGroupId());
        }

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currentGroup", currentGroup);
        infoMap.put("name", name);
        return infoMap;
    }
}
