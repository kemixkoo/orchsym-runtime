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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.controller.Template;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.StandardNiFiServiceFacade;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.SnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.AppGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.apache.nifi.web.api.entity.SnippetEntity;
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
@Path("/application")
@Api(value = "/application", description = "app search")
public class OrchsymApplicationResource extends AbsOrchsymResource {
    /**
     * @apiNote group中相关的创建和修改时间
     */
    private static final String createdTime = StandardNiFiServiceFacade.createdTime;
    private static final String modifiedTime = StandardNiFiServiceFacade.modifiedTime;
    
    private static final String IS_DELETED = "IS_DELETED";
    private static final String IS_ENABLED = "IS_ENABLED";

    @Autowired
    private FlowService flowService;

    @Autowired
    private RevisionManager revisionManager;

    private Response verifyApp(String appId) {
        boolean existed = flowController.getRootGroup().getProcessGroups().stream().filter(group -> group.getIdentifier().equals(appId)).findAny().isPresent();
        if (existed) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }
        return null;
    }
    /**
     * 为APP实体类赋值
     * @param groupEntity
     * @param groupId
     */
    private void setTimeStampForApp(AppGroupEntity groupEntity, String  groupId){
        final ProcessGroup group = flowController.getGroup(groupId);
        if (group == null){
            return;
        }
        groupEntity.setId(group.getIdentifier());
        groupEntity.setName(group.getName());
        groupEntity.setComments(group.getComments());

        final Map<String, String> additions = group.getAdditions();
        if (additions != null && additions.containsKey(createdTime)){
            long createTime = Long.parseLong(additions.get(createdTime));
            groupEntity.setCreatedTime(createTime);
        }

        if (additions != null && additions.containsKey(modifiedTime)){
            long modifyTime = Long.parseLong(additions.get(modifiedTime));
            groupEntity.setModifiedTime(modifyTime);
        }

        if (additions != null){
            if (Boolean.parseBoolean(additions.get(IS_DELETED))){
                groupEntity.setDeleted(true);
            }else {
                groupEntity.setDeleted(false);
            }
        }

    }

    /**
     *
     * @param value
     * @param page
     * @param pageSize
     * @param sortedField 有以下几个取值类型: name createdTime modifiedTime 名称、创建时间、修改时间
     * @return
     * @throws InterruptedException
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchFlowAPPGroup(
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String value,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sortedField") @DefaultValue("name") String sortedField,
            @QueryParam("isDesc") @DefaultValue("true") Boolean isDesc,
            @QueryParam("isDeleted") @DefaultValue("false") Boolean isDeleted,
            @QueryParam("isDetail") @DefaultValue("false") Boolean isDetail

    ) throws InterruptedException {

        List<AppGroupEntity> appGroupEntityList = new ArrayList<>();

        // 进行数据封装抽取
        final SearchResultsDTO results = serviceFacade.searchAppsOfController(value,flowController.getRootGroupId());
        final List<ComponentSearchResultDTO> processGroupResults = results.getProcessGroupResults();
        for (ComponentSearchResultDTO dto : processGroupResults){
            final AppGroupEntity appGroupEntity = new AppGroupEntity();
            setTimeStampForApp(appGroupEntity, dto.getId());
            appGroupEntityList.add(appGroupEntity);
        }

        // 进行筛选
        appGroupEntityList = appGroupEntityList.stream().filter(appGroupEntity -> appGroupEntity.getDeleted().equals(isDeleted)).collect(Collectors.toList());

        // 进行排序
        Collections.sort(appGroupEntityList, new Comparator<AppGroupEntity>() {
            @Override
            public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                if ("createdTime".equalsIgnoreCase(sortedField) //
                        && o1.getCreatedTime() != null && o2.getCreatedTime() != null) {
                    return isDesc ? o2.getCreatedTime().compareTo(o1.getCreatedTime()) : o1.getCreatedTime().compareTo(o2.getCreatedTime());
                } else if ("modifiedTime".equalsIgnoreCase(sortedField) //
                        && o1.getModifiedTime() != null && o2.getModifiedTime() != null) {
                    return isDesc ? o2.getModifiedTime().compareTo(o1.getModifiedTime()) : o1.getModifiedTime().compareTo(o2.getModifiedTime());
                } else {
                    return isDesc ? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                }
            }
        });


        // 处理分页
        // 总条数 与 总页数
        int totalSize = appGroupEntityList.size();
        int totalPage = (totalSize + pageSize - 1) / pageSize;
        int index = (page - 1) * pageSize;
        int currentPage = page;

        List<AppGroupEntity> resultList = null;
        if (index >= totalSize ){
            resultList = new ArrayList<>();
        }else {
            int endIndex = Math.min(index + pageSize, totalSize);
            resultList = appGroupEntityList.subList(index, endIndex);
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("totalSize",totalSize);
        resultMap.put("totalPage",totalPage);
        resultMap.put("currentPage",currentPage);

        if (isDetail){
            List<ProcessGroupEntity>  entities = new ArrayList<>();
            for (AppGroupEntity app : resultList){
                entities.add(serviceFacade.getProcessGroup(app.getId()));
            }
            resultMap.put("results", entities);
        }else {
            resultMap.put("results", resultList);
        }

        // generate the response
        return noCache(Response.ok(resultMap)).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/group/{groupId}/search-results")
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

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/check_name")
    @ApiOperation(value = "check the name of current app", //
            response = Map.class)
    public Response checkAppName(//
            @QueryParam("name") String name, //
            @QueryParam("appId") String appId//
    ) {
        boolean isAppNameValid = true;
        if (StringUtils.isBlank(name)) {
            isAppNameValid = false;
        } else {
            isAppNameValid = !flowController.getRootGroup().getProcessGroups().stream() //
                    .filter(g -> StringUtils.isBlank(appId) || !g.getIdentifier().equals(appId)) // exclude
                    .filter(p -> p.getName().equals(name))// existed
                    .findFirst() //
                    .isPresent();//
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("isValid", isAppNameValid);
        return noCache(Response.ok(resultMap)).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/status")
    @ApiOperation(value = "Get the status of current app", //
            response = Map.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAppStatus(@PathParam("appId") final String appId) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        final ProcessGroup groupApp = flowController.getGroup(appId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", groupApp.getIdentifier());

        // running/stopped
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put(runCount, 0);
        countMap.put(stoppedCount, 0);
        AtomicReference<Boolean> isStopped = new AtomicReference<>(false);
        collectStatusCountOfAppByDirectName(appId, isStopped, countMap);
        resultMap.put("canRun", countMap.get(stoppedCount) > 0);
        resultMap.put("canStop", countMap.get(runCount) > 0);

        // enabled/disabled
        // 为兼容老版本，不设置，默认为enabled
        boolean isEnabled = ProcessUtil.getGroupAdditionBooleanValue(groupApp, IS_ENABLED, true);
        resultMap.put("canEnable", !isEnabled);
        resultMap.put("canDisable", isEnabled);

        return noCache(Response.ok(resultMap)).build();
    }

    private static final String runCount = "runCount";
    private static final String stoppedCount = "stoppedCount";

    private void collectStatusCountOfAppByDirectName(String groupId, AtomicReference<Boolean> isStopped, Map<String, Integer> countMap) {
        if (isStopped.get()) {
            return;
        }
        final ProcessGroup group = flowController.getGroup(groupId);
        if (group == null) {
            return;
        }

        // 一旦识别所有状态，立即返回
        if (countMap.get(runCount) != 0 && countMap.get(stoppedCount) != 0) {
            isStopped.set(true);
            return;
        }

        for (final ProcessorNode processor : group.getProcessors()) {
            final ScheduledState state = processor.getScheduledState();
            if (state.equals(ScheduledState.RUNNING)) {
                countMap.put(runCount, countMap.get(runCount) + 1);
            } else if (state.equals(ScheduledState.STOPPED)) {
                countMap.put(stoppedCount, countMap.get(stoppedCount) + 1);
            }
        }

        for (final ControllerServiceNode service : group.getControllerServices(false)) {
            final ControllerServiceState state = service.getState();
            if (state.equals(ControllerServiceState.ENABLED)) {
                countMap.put(runCount, countMap.get(runCount) + 1);
            } else if (state.equals(ControllerServiceState.DISABLED)) {
                countMap.put(stoppedCount, countMap.get(stoppedCount) + 1);
            }
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            collectStatusCountOfAppByDirectName(childGroup.getIdentifier(), isStopped, countMap);
        }

    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/logic_delete")
    @ApiOperation(value = "delete the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response loginDeleteApp(@PathParam("appId") final String appId) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }

        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(appId);

        return withWriteLock(serviceFacade, groupEntity, lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(appId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        }, null, (entity) -> {
            final ProcessGroup group = flowController.getGroup(entity.getId());
            deleteGroupLogic(group);
            saveAppStatus(entity.getId(), IS_DELETED, Boolean.TRUE);
            return generateOkResponse("success").build();
        });
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/physical_delete")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response physicalDeleteApp(@PathParam("appId") String appId) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        return forceDeleteGroup(appId);
    }

    private void deleteGroupLogic(ProcessGroup group) {
        for (ProcessorNode processorNode : group.getProcessors()) {
            final ProcessorDTO processorDTO = new ProcessorDTO();
            processorDTO.setId(processorNode.getIdentifier());
            processorDTO.setState(ScheduleComponentsEntity.STATE_STOPPED);
            Revision revision = revisionManager.getRevision(processorNode.getIdentifier());
            serviceFacade.updateProcessor(revision, processorDTO);
        }

        for (Connection connection : group.getConnections()) {
            DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(connection.getIdentifier(), generateUuid());
            serviceFacade.deleteFlowFileDropRequest(connection.getIdentifier(), dropRequest.getId());
        }

        for (ControllerServiceNode controllerServiceNode : group.getControllerServices(false)) {
            Revision revision = revisionManager.getRevision(controllerServiceNode.getIdentifier());
            ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
            controllerServiceDTO.setId(controllerServiceNode.getIdentifier());
            controllerServiceDTO.setState(ScheduleComponentsEntity.STATE_DISABLED);
            serviceFacade.updateControllerService(revision, controllerServiceDTO);
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            deleteGroupLogic(childGroup);
        }
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/recover")
    @ApiOperation(value = "recover the app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response recoverApp(@Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        return updateAppStatus(appId, IS_DELETED, Boolean.FALSE);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/enable")
    @ApiOperation(value = "enable the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response enableApp(@Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        return updateAppStatus(appId, IS_ENABLED, Boolean.TRUE);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/disable")
    @ApiOperation(value = "disable the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response disableApp(@Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        // TODO, need like logic delete or not? or just stop the service, won't clean data queue?
        return updateAppStatus(appId, IS_ENABLED, Boolean.FALSE);
    }

    private Response updateAppStatus(final String appId, final String additionKey, final Object value) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        }

        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(appId);

        return withWriteLock(serviceFacade, groupEntity, lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(appId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
        }, null, (entity) -> {
            saveAppStatus(entity.getId(), additionKey, value);
            return generateOkResponse("success").build();
        });
    }

    private void saveAppStatus(String appId, String key, Object value) {
        final ProcessGroup group = flowController.getGroup(appId);

        ProcessUtil.updateGroupAdditions(group, key, value);

        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/template")
    @ApiOperation(value = "Get the template data of current app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getAppTemplateData(
            @PathParam("groupId") String groupId
    ) {

        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        final ProcessGroup groupApp = flowController.getGroup(groupId);
        if (groupApp == null){
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        // create snipped
        final Revision revision = revisionManager.getRevision(groupId);
        SnippetDTO snippetDTO = new SnippetDTO();
        Map<String, RevisionDTO> revisionMap = new HashMap<>();
        RevisionDTO revisionDTO = new RevisionDTO();
        revisionDTO.setClientId(revision.getClientId());
        revisionDTO.setVersion(revision.getVersion());
        revisionMap.put(groupId,revisionDTO);
        snippetDTO.setProcessGroups(revisionMap);
        snippetDTO.setId(generateUuid());
        snippetDTO.setParentGroupId(flowController.getRootGroupId());
        final SnippetEntity snippetEntity = serviceFacade.createSnippet(snippetDTO);

        // generate data of template
        final String snippetId = snippetEntity.getSnippet().getId();
        TemplateDTO templateDTO = serviceFacade.createTemplate(groupApp.getName(), groupApp.getComments(),
                snippetId, flowController.getRootGroupId(), getIdGenerationSeed());
        final Template template = new Template(templateDTO);
        final TemplateDTO templateCopy = serviceFacade.exportTemplate(template.getIdentifier());
        flowController.getRootGroup().removeTemplate(template);
        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
        templateCopy.setId(null);

        return noCache(Response.ok(templateCopy)).build();
    }


    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/group/{groupId}/force_delete")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response forceDeleteGroup(
            @PathParam("groupId") String groupId
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }

        final ProcessGroup groupApp = flowController.getGroup(groupId);
        if (groupApp == null){
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(groupApp.getIdentifier());

        return withWriteLock(
                serviceFacade,
                groupEntity,
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                },
                null,
                (entity) -> {
                    final ProcessGroup group = flowController.getGroup(entity.getId());
                    // 先进行逻辑删除
                    deleteGroupLogic(group);
                    // 校验
                    serviceFacade.verifyDeleteProcessGroup(entity.getId());
                    // 物理删除
                    serviceFacade.deleteProcessGroup(revisionManager.getRevision(entity.getId()), entity.getId());
                    return generateOkResponse("success").build();
                }
        );
    }


}
