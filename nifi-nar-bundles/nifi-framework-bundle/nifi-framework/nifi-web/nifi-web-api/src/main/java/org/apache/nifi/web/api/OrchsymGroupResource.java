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

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.apache.nifi.controller.*;
import org.apache.nifi.controller.label.Label;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.common.OrchsymCommon;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.SnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.*;
import org.apache.nifi.web.api.orchsym.group.AllComponentIdInGroup;
import org.apache.nifi.web.api.orchsym.template.TemplateFieldName;
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
    @Autowired
    private FlowService flowService;

    @Autowired
    private OrchsymCommon orchsymCommon;

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
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{groupId}/force_delete")
    @ApiOperation(value = "delete the group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response forceDeleteGroup(//
            @PathParam("groupId") String groupId//
    ) {
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

        return withWriteLock(//
                serviceFacade, //
                groupEntity, //
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(groupEntity.getId()).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                }, //
                null, //
                (entity) -> {
                    final ProcessGroup group = flowController.getGroup(entity.getId());
                    // 为保证删除成功，清理数据
                    safeCleanGroup(group, true, true, true, true);
                    // 校验
                    serviceFacade.verifyDeleteProcessGroup(entity.getId());
                    // 物理删除
                    serviceFacade.deleteProcessGroup(revisionManager.getRevision(entity.getId()), entity.getId());
                    return generateOkResponse("success").build();
                });
    }


    void safeCleanGroup(ProcessGroup group, boolean stopComponents, boolean stopServices, boolean cleanQueue, boolean removeTemplates) {
        final AllComponentIdInGroup allIdsInGroup = orchsymCommon.getAllComponentIdsInGroup(group.getIdentifier());
        if (stopComponents) { // 停止所有组件
            for (String processorId : allIdsInGroup.getProcessorIds()){
                final ProcessorDTO processorDTO = new ProcessorDTO();
                processorDTO.setId(processorId);
                processorDTO.setState(ScheduleComponentsEntity.STATE_STOPPED);
                Revision revision = revisionManager.getRevision(processorId);
                serviceFacade.updateProcessor(revision, processorDTO);
            }
        }
        // 如果组件未终止 需要强制等待
        int times = 30;
        // 轮询间隔 0.1秒
        long timeUnit = 100;
        int count = 0;
        while (!isAllProcessorStoppped(group) && count < times){
            try {
                Thread.sleep(timeUnit);
                count++;
            } catch (InterruptedException e) {
                //
            }
        }

        if (count >= times){
            throw new IllegalStateException("the time of stopping processors too long, please retry");
        }

        if (cleanQueue) { // 清空队列
            for (String connectionId : allIdsInGroup.getConnectionIds()){
                DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(connectionId, generateUuid());
                serviceFacade.deleteFlowFileDropRequest(connectionId, dropRequest.getId());
            }
        }
        if (removeTemplates) { // 删除应用内所有模块下的模板
            for (String templateId : allIdsInGroup.getTemplateIds()){
                serviceFacade.deleteTemplate(templateId);
            }
        }

        if (stopServices) { // 停止服务
            for (String serviceId : allIdsInGroup.getServiceIds()){
                disableServiceByIdentifier(serviceId);
            }
        }
    }

    private boolean isAllProcessorStoppped(ProcessGroup group){
     return group.getProcessors().stream().noneMatch(p -> {
         final ScheduledState state = p.getPhysicalScheduledState();
         return !state.equals(ScheduledState.DISABLED) && !state.equals(ScheduledState.STOPPED);
     });
    }

    /**
     * 禁用服务 涉及到自身被引用的服务及其组件 需单独处理
     * @param controllerServiceId
     */
    private void disableServiceByIdentifier(final String controllerServiceId){
        final ControllerServiceEntity controllerServiceToDisable = serviceFacade.getControllerService(controllerServiceId);
        if (controllerServiceToDisable.getComponent().getState().equalsIgnoreCase(ControllerServiceState.DISABLED.name())) {
            return ;
        }

        final Set<ControllerServiceReferencingComponentEntity> referencingComponentEntities = serviceFacade.getControllerServiceReferencingComponents(controllerServiceId)
                .getControllerServiceReferencingComponents();
        final Map<String, Revision> referencingRevisions = referencingComponentEntities.stream().collect(Collectors.toMap(ControllerServiceReferencingComponentEntity::getId, entity -> {
            final Revision revision = revisionManager.getRevision(entity.getId());
            return revision;
        }));

        serviceFacade.updateControllerServiceReferencingComponents(referencingRevisions, controllerServiceId, ScheduledState.STOPPED, null);

        serviceFacade.updateControllerServiceReferencingComponents(new HashMap<>(), controllerServiceId, ScheduledState.DISABLED, ControllerServiceState.DISABLED);

        final ControllerServiceDTO serviceDTO = new ControllerServiceDTO();
        serviceDTO.setId(controllerServiceId);
        serviceDTO.setState(ControllerServiceState.DISABLED.name());

        final Revision revision = revisionManager.getRevision(serviceDTO.getId());
        serviceFacade.updateControllerService(revision, serviceDTO);

    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{id}/navigator")
    @ApiOperation(value = "Get the parents info of current app or component", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getNavigatorInfo(//
            @PathParam("id") String id//
    ) {
        final Object component = getComponentById(id);
        if (component == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ComponentInfo compInfo = getCurrentGroup(component);
        if (Objects.isNull(compInfo.currentGroup)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final String currentGroupId = compInfo.currentGroup.getIdentifier();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", currentGroupId);
        resultMap.put("name", compInfo.componentName);
        if (!compInfo.currentGroup.isRootGroup()) {
            final ProcessGroup parentGroup = compInfo.currentGroup.getParent();
            if (!Objects.isNull(parentGroup) && !parentGroup.isRootGroup()) {// 当前模块为非应用解包
                final boolean groupSelf = currentGroupId.equals(id);
                // 当前选择的是非模块，则父模块则为当前模块
                resultMap.put("parentGroupId", groupSelf ? parentGroup.getIdentifier() : currentGroupId);
                resultMap.put("parentGroupName", groupSelf ? parentGroup.getName() : compInfo.currentGroup.getName());
            }
        }

        ProcessGroup tempGroup = compInfo.currentGroup;
        List<Map<String, String>> groups = new ArrayList<>();
        while (!tempGroup.isRootGroup()) {
            Map<String, String> groupInfoMap = new HashMap<>();
            groupInfoMap.put("id", tempGroup.getIdentifier());
            groupInfoMap.put("name", tempGroup.getName());
            if (!tempGroup.getParent().isRootGroup()) {//仅到应用级别
                groupInfoMap.put("parentGroupId", tempGroup.getParent().getIdentifier());
            }
            groups.add(groupInfoMap);
            tempGroup = tempGroup.getParent();
        }
        if (groups.size() > 0) {
            Collections.reverse(groups);

            final String path = StringUtils.join(groups.stream().map(map -> map.getOrDefault("name", "")).collect(Collectors.toList()), "/");
            resultMap.put("path", path);
            resultMap.put("groups", groups);

            // 第一个即为应用级别的模块
            ProcessGroup appGroup = flowController.getGroup(groups.get(0).get("id"));
            resultMap.put("applicationId", appGroup.getIdentifier());
            resultMap.put("applicationName", appGroup.getName());

        }

        return Response.ok(resultMap).build();
    }

    static class ComponentInfo {
        String componentName;
        ProcessGroup currentGroup;// 当前Component的模块，
        String parentGroupId, parentGroupName;// 包含该Component的模块

    }

    private ComponentInfo getCurrentGroup(Object component) {
        ComponentInfo compInfo = new ComponentInfo();

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
        if (!Objects.isNull(currentGroup)) {
            compInfo.currentGroup = currentGroup;

            if (currentGroup.isRootGroup()) {
                compInfo.componentName = FlowController.ROOT_GROUP_ID_ALIAS;
            } else {
                compInfo.componentName = name;
            }
        }
        return compInfo;
    }

    /**
     * 
     * 模块下载为模板的数据生成
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/template/{groupId}/data")
    @ApiOperation(value = "Get the template data of current app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response generateTemplateData(//
            @PathParam("groupId") String groupId//
    ) {
        final ProcessGroup groupApp = flowController.getGroup(groupId);
        if (groupApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        if (isReplicateRequest()) {
            // GET请求集群转发 没有write-Lock 也不会报错
            return replicate(HttpMethod.GET);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }

        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        // create snippet
        final Revision revision = revisionManager.getRevision(groupId);
        SnippetDTO snippetDTO = new SnippetDTO();
        Map<String, RevisionDTO> revisionMap = new HashMap<>();
        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.setClientId(revision.getClientId());
        revisionDTO.setVersion(revision.getVersion());
        revisionMap.put(groupId, revisionDTO);
        snippetDTO.setProcessGroups(revisionMap);
        snippetDTO.setId(generateUuid());
        snippetDTO.setParentGroupId(flowController.getRootGroupId());

        final SnippetEntity snippetEntity = serviceFacade.createSnippet(snippetDTO);

        // generate data of template
        final String snippetId = snippetEntity.getSnippet().getId();
        final String tmpTemplateName = UUID.randomUUID().toString(); // 随机生成名字，防止名字同名冲突而无法创建临时模板
        TemplateDTO templateDTO = serviceFacade.createTemplate(tmpTemplateName, groupApp.getComments(), snippetId, flowController.getRootGroupId(), getIdGenerationSeed());

        // 取消直接持久化 改为直接复制
        final Template template = new Template(templateDTO);
        final TemplateDTO templateCopy = serviceFacade.exportTemplate(template.getIdentifier());
        flowController.getRootGroup().removeTemplate(template);
        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
        templateCopy.setId(null);

        // 构建默认设置
        Map<String, String> additionsMap = TemplateFieldName.getCreatedAdditions(null, groupApp.getParent().isRootGroup(), NiFiUserUtils.getNiFiUserIdentity());
        templateCopy.setAdditions(additionsMap);
        templateCopy.setTags(groupApp.getTags());
        // rewrite app group name to data template
        templateCopy.setName(groupApp.getName());
        return noCache(Response.ok(templateCopy)).build();
    }

}
