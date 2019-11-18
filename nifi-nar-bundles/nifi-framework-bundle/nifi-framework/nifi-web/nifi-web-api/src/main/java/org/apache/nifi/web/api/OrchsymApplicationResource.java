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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
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
import org.apache.nifi.authorization.SnippetAuthorizable;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.connectable.ConnectableType;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.connectable.Funnel;
import org.apache.nifi.connectable.Port;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.controller.Snippet;
import org.apache.nifi.controller.Template;
import org.apache.nifi.controller.label.Label;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.templates.TemplateFiledName;
import org.apache.nifi.templates.TemplateSourceType;
import org.apache.nifi.util.PositionCalcUtil;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.StandardNiFiServiceFacade;
import org.apache.nifi.web.api.dto.AppCopyDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.SnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.AppCopyEntity;
import org.apache.nifi.web.api.entity.AppGroupEntity;
import org.apache.nifi.web.api.entity.AppSearchEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.apache.nifi.web.api.entity.SnippetEntity;
import org.apache.nifi.web.revision.RevisionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.lang.Objects;
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
@Api(value = "/application", description = "app API")
public class OrchsymApplicationResource extends AbsOrchsymResource {
    /**
     * @apiNote group中相关的创建和修改时间
     */
    private static final String KEY_CREATED_TIME = StandardNiFiServiceFacade.createdTime;
    private static final String KEY_MODIFIED_TIME = StandardNiFiServiceFacade.modifiedTime;
    private static final String PARAM_MODIFIED_TIME = "modifiedTime";
    private static final String PARAM_CREATED_TIME = "createdTime";

    private static final String IS_DELETED = "IS_DELETED";
    private static final String IS_ENABLED = "IS_ENABLED";

    @Autowired
    private FlowService flowService;

    @Autowired
    private RevisionManager revisionManager;

    @Autowired
    private OrchsymGroupResource groupResource;

    private Response verifyApp(String appId) {
        boolean existed = flowController.getRootGroup().getProcessGroups().stream().filter(group -> group.getIdentifier().equals(appId)).findAny().isPresent();
        if (!existed) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }
        return null;
    }

    /**
     *
     * @param text
     * @param page
     * @param pageSize
     * @param sortedField
     *            有以下几个取值类型: name createdTime modifiedTime 名称、创建时间、修改时间
     * @return
     * @throws InterruptedException
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchApp(//
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String text, //

            //
            @QueryParam("page") Integer currentPage, //
            @QueryParam("pageSize") Integer pageSize, //

            // sort
            @QueryParam("sortedField") String sortedField, //
            @QueryParam("isDesc") Boolean isDesc, //

            // filter
            @QueryParam("isDeleted") Boolean isDeleted, //
            @QueryParam("isEnabled") Boolean isEnabled, //
            @QueryParam("isRunning") Boolean isRunning, //
            @QueryParam("hasDataQueue") Boolean hasDataQueue, //
            @QueryParam("timeField") String timeField, //
            @QueryParam("beginTime") Long beginTime, //
            @QueryParam("endTime") Long endTime, //
            @QueryParam("tags") String tags, // 英文逗号分隔多个

            //
            @QueryParam("isDetail") Boolean needDetail //

    ) throws InterruptedException {
        AppSearchEntity searchEnity = new AppSearchEntity();
        searchEnity.setText(text);
        searchEnity.setCurrentPage(currentPage);
        searchEnity.setPageSize(pageSize);
        searchEnity.setSortedField(sortedField);
        searchEnity.setIsDesc(isDesc);
        searchEnity.setDeleted(isDeleted);
        searchEnity.setEnabled(isEnabled);
        searchEnity.setIsRunning(isRunning);
        searchEnity.setHasDataQueue(hasDataQueue);
        searchEnity.setFilterTimeField(timeField);
        searchEnity.setBeginTime(beginTime);
        searchEnity.setEndTime(endTime);
        searchEnity.setTags(null != tags ? Arrays.asList(tags.split(",")).stream().filter(t -> StringUtils.isNotBlank(t)).map(t -> t.trim()).collect(Collectors.toSet()) : Collections.emptySet());
        searchEnity.setNeedDetail(needDetail);

        return searchApp(searchEnity);
    }

    private static final String DEFAULT_SORT_FIELD = "name";
    private static final boolean DEFAULT_SORT_DESC = true;

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;// 每页10

    private static final boolean DEFAULT_NEED_DETAIL = false;// 无详情

    private static final Function<? super String, ? extends String> FUN_LOWERCASE = t -> t.toLowerCase();

    private void fixDefaultSearchEnity(final AppSearchEntity searchEnity) {
        // 排序默认值
        if (null == searchEnity.getSortedField()) {
            searchEnity.setSortedField(DEFAULT_SORT_FIELD);
        }
        if (null == searchEnity.getIsDesc()) {
            searchEnity.setIsDesc(DEFAULT_SORT_DESC);
        }

        // 分页默认值
        if (null == searchEnity.getCurrentPage()) {
            searchEnity.setCurrentPage(DEFAULT_PAGE);
        }
        if (null == searchEnity.getPageSize()) {
            searchEnity.setPageSize(DEFAULT_PAGE_SIZE);
        }

        if (null == searchEnity.getNeedDetail()) {
            searchEnity.setNeedDetail(DEFAULT_NEED_DETAIL);
        }

        // FIXME, 暂不支持modified_time，否应该删除统一到创建日期上
        if (PARAM_MODIFIED_TIME.equals(searchEnity.getSortedField())) {
            searchEnity.setSortedField(PARAM_CREATED_TIME);
        }
        if (PARAM_MODIFIED_TIME.equals(searchEnity.getFilterTimeField())) {
            searchEnity.setFilterTimeField(PARAM_CREATED_TIME);
        }

    }

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchApp(final AppSearchEntity searchEnity) throws InterruptedException {
        fixDefaultSearchEnity(searchEnity);

        // 搜索
        final SearchResultsDTO results = serviceFacade.searchAppsOfController(searchEnity.getText(), flowController.getRootGroupId());
        // 数据封装
        List<AppGroupEntity> appGroupEntityList = results.getProcessGroupResults().stream() //
                .map(dto -> {
                    final AppGroupEntity groupEntity = new AppGroupEntity();
                    final ProcessGroup group = flowController.getGroup(dto.getId());
                    if (null != group) {
                        groupEntity.setId(group.getIdentifier());
                        groupEntity.setName(group.getName());
                        groupEntity.setComments(group.getComments());

                        groupEntity.setCreatedTime(ProcessUtil.getGroupAdditionLongValue(group, KEY_CREATED_TIME));
                        groupEntity.setModifiedTime(ProcessUtil.getGroupAdditionLongValue(group, KEY_MODIFIED_TIME));

                        groupEntity.setDeleted(ProcessUtil.getGroupAdditionBooleanValue(group, IS_DELETED));
                        groupEntity.setEnabled(ProcessUtil.getGroupAdditionBooleanValue(group, IS_ENABLED));

                        if (null != group.getTags()) {
                            groupEntity.setTags(new HashSet<>(group.getTags()));
                        }
                    }

                    return groupEntity;
                }).collect(Collectors.toList());

        // 进行筛选
        final Boolean deleted = searchEnity.getDeleted();
        final Boolean enabled = searchEnity.getEnabled();
        final Boolean isRunning = searchEnity.getIsRunning();
        final Boolean hasDataQueue = searchEnity.getHasDataQueue();
        final Set<String> tags = searchEnity.getTags();
        final String filterTimeField = searchEnity.getFilterTimeField();
        final Long beginTime = searchEnity.getBeginTime();
        final Long endTime = searchEnity.getEndTime();

        appGroupEntityList = appGroupEntityList.stream().filter(appGroupEntity -> {
            if (null != deleted && !appGroupEntity.isDeleted().equals(deleted)) {
                return false;
            }
            if (null != enabled && !appGroupEntity.isEnabled().equals(enabled)) {
                return false;
            }
            if (null != isRunning) {
                final ProcessGroupEntity groupEntity = serviceFacade.getProcessGroup(appGroupEntity.getId());
                if (!isRunning.equals((groupEntity.getRunningCount() > 0))) {
                    return false;
                }
            }
            if (null != hasDataQueue && !isGroupHasDataQueue(appGroupEntity.getId()).equals(hasDataQueue)) {
                return false;
            }
            if (null != tags) {
                final Set<String> tagList = tags.stream()//
                        .filter(t -> StringUtils.isNotBlank(t))//
                        .map(FUN_LOWERCASE)//
                        .collect(Collectors.toSet());
                if (tagList.size() > 0) { // 设置了tag过滤
                    Set<String> appTags = (null != appGroupEntity.getTags()) ? appGroupEntity.getTags() : Collections.emptySet();
                    boolean existed = appTags.stream().map(FUN_LOWERCASE).filter(t -> tagList.contains(t)).findAny().isPresent();
                    if (!existed) {
                        return false;
                    }
                }
            }

            if (null != filterTimeField && (beginTime != null || endTime != null)) {
                if (PARAM_CREATED_TIME.equals(filterTimeField)) {
                    final Long createdTime = appGroupEntity.getCreatedTime();
                    if (beginTime != null && createdTime < beginTime) {
                        return false;
                    }
                    if (endTime != null && createdTime > endTime) {
                        return false;
                    }
                } else if (PARAM_MODIFIED_TIME.equals(filterTimeField)) {
                    final Long modifiedTime = appGroupEntity.getModifiedTime();
                    if (beginTime != null && modifiedTime < beginTime) {
                        return false;
                    }

                    if (endTime != null && modifiedTime > endTime) {
                        return false;
                    }
                }
            }
            return true;
        }).collect(Collectors.toList());

        // 进行排序
        final String sortField = searchEnity.getSortedField();
        final Boolean isDesc = searchEnity.getIsDesc();
        Collections.sort(appGroupEntityList, new Comparator<AppGroupEntity>() {
            @Override
            public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                if (PARAM_CREATED_TIME.equalsIgnoreCase(sortField) && o1.getCreatedTime() != null && o2.getCreatedTime() != null) {
                    return isDesc ? o2.getCreatedTime().compareTo(o1.getCreatedTime()) : o1.getCreatedTime().compareTo(o2.getCreatedTime());
                } else if (PARAM_MODIFIED_TIME.equalsIgnoreCase(sortField) && o1.getModifiedTime() != null && o2.getModifiedTime() != null) {
                    return isDesc ? o2.getModifiedTime().compareTo(o1.getModifiedTime()) : o1.getModifiedTime().compareTo(o2.getModifiedTime());
                } else {
                    return isDesc ? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                }
            }
        });

        // 处理分页
        // 总条数 与 总页数
        final Integer pageSize = searchEnity.getPageSize();
        final Integer currentPage = searchEnity.getCurrentPage();
        int totalSize = appGroupEntityList.size();
        int totalPage = (totalSize + pageSize - 1) / pageSize;
        int index = (currentPage - 1) * pageSize;

        List<AppGroupEntity> resultList = null;
        if (index >= totalSize) {
            resultList = new ArrayList<>();
        } else {
            int endIndex = Math.min(index + pageSize, totalSize);
            resultList = appGroupEntityList.subList(index, endIndex);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalSize", totalSize);
        resultMap.put("totalPage", totalPage);
        resultMap.put("currentPage", currentPage);

        if (null != searchEnity.getNeedDetail() && searchEnity.getNeedDetail()) {
            List<ProcessGroupEntity> entities = new ArrayList<>();
            for (AppGroupEntity app : resultList) {
                entities.add(serviceFacade.getProcessGroup(app.getId()));
            }
            resultMap.put("results", entities);
        } else {
            resultMap.put("results", resultList);
        }

        // generate the response
        return noCache(Response.ok(resultMap)).build();
    }

    private Boolean isGroupHasDataQueue(String groupId) {
        final ProcessGroup group = flowController.getGroup(groupId);
        AtomicReference<Boolean> hasDataQueue = new AtomicReference<>(false);
        verifyHasDataQueue(group, hasDataQueue);
        return hasDataQueue.get();
    }

    private void verifyHasDataQueue(ProcessGroup group, AtomicReference<Boolean> hasDataQueue) {
        if (hasDataQueue.get()) {
            return;
        }
        for (Connection connection : group.getConnections()) {
            if (!connection.getFlowFileQueue().isEmpty()) {
                hasDataQueue.set(true);
                return;
            }
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            verifyHasDataQueue(childGroup, hasDataQueue);
        }
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check_name")
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

    private static final boolean DEFAULT_FILTER_DELETED = false; // 未删除
    private static final boolean DEFAULT_FILTER_ENABLED = true; // 可用

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appId}/status")
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
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
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
        boolean isEnabled = ProcessUtil.getGroupAdditionBooleanValue(groupApp, IS_ENABLED, DEFAULT_FILTER_ENABLED);
        resultMap.put("canEnable", !isEnabled);
        resultMap.put("canDisable", isEnabled);

        boolean isDeleted = ProcessUtil.getGroupAdditionBooleanValue(groupApp, IS_DELETED, DEFAULT_FILTER_DELETED);
        resultMap.put("canDelete", !isDeleted);

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
    @Path("/{appId}/logic_delete")
    @ApiOperation(value = "delete the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response logicDeleteApp(@PathParam("appId") final String appId) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }

        return getResponseForLogicDeleteApp(flowController.getGroup(appId));
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logic_delete_by_name")
    @ApiOperation(value = "delete the app via name logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response logicDeleteApp(final AppGroupEntity appGroupEntity) {
        if (appGroupEntity == null || appGroupEntity.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("param cant be null and mast contains 'name'").build();
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, appGroupEntity);
        }

        final Optional<ProcessGroup> findFirst = flowController.getRootGroup().getProcessGroups().stream().filter(group -> group.getName().equals(appGroupEntity.getName())).findFirst();
        if (!findFirst.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the app by the appName" + "'" + appGroupEntity.getName() + "'").build();
        }

        return getResponseForLogicDeleteApp(findFirst.get());
    }

    private Response getResponseForLogicDeleteApp(ProcessGroup pg) {
        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(pg.getIdentifier());

        return withWriteLock(serviceFacade, groupEntity, lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(groupEntity.getId()).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        }, null, (entity) -> {
            final ProcessGroup group = flowController.getGroup(entity.getId());
            deleteGroupLogic(group);
            saveAppStatus(entity.getId(), IS_DELETED, Boolean.TRUE);
            return generateOkResponse("success").build();
        });
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
    @Path("/{appId}/recover")
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
    @Path("/{appId}/enable")
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
    @Path("/{appId}/disable")
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
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAppTemplateData(@PathParam("groupId") String groupId) {

        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }

        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });

        final ProcessGroup groupApp = flowController.getGroup(groupId);
        if (groupApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        // create snipped
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
        TemplateDTO templateDTO = serviceFacade.createTemplate(groupApp.getName(), groupApp.getComments(), snippetId, flowController.getRootGroupId(), getIdGenerationSeed());

        final Template template = new Template(templateDTO);
        final TemplateDTO templateCopy = serviceFacade.exportTemplate(template.getIdentifier());
        flowController.getRootGroup().removeTemplate(template);
        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
        templateCopy.setId(null);

        // 取消直接持久化 改为直接复制
        Map<String, String> additionsMap = new HashMap<>();
        additionsMap.put(TemplateFiledName.CREATED_TIME, Long.toString(System.currentTimeMillis()));
        additionsMap.put(TemplateFiledName.CREATED_USER, NiFiUserUtils.getNiFiUserIdentity());
        additionsMap.put(TemplateFiledName.SOURCE_TYPE, Integer.toString(TemplateSourceType.SAVE_AS_TYPE.value()));
        templateCopy.setAdditions(additionsMap);
        templateCopy.setTags(groupApp.getTags());
        return noCache(Response.ok(templateCopy)).build();
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appId}/force_delete")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response forceDeleteApp(@PathParam("appId") String appId) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        return groupResource.forceDeleteGroup(appId);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/force_delete_by_name")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response forceDeleteAppByName(final AppGroupEntity appGroupEntity) {
        if (appGroupEntity == null || appGroupEntity.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("param cant be null and mast contains 'name'").build();
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, appGroupEntity);
        }

        final Optional<ProcessGroup> findFirst = flowController.getRootGroup().getProcessGroups().stream().filter(group -> group.getName().equals(appGroupEntity.getName())).findFirst();
        if (!findFirst.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the app by the appName" + "'" + appGroupEntity.getName() + "'").build();
        }

        return getResponseForForceDeleteGroup(findFirst.get());
    }

    private Response getResponseForForceDeleteGroup(ProcessGroup gp) {
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
                    // 先进行逻辑删除
                    deleteGroupLogic(group);
                    // 校验
                    serviceFacade.verifyDeleteProcessGroup(entity.getId());
                    // 物理删除
                    serviceFacade.deleteProcessGroup(revisionManager.getRevision(entity.getId()), entity.getId());
                    return generateOkResponse("success").build();
                });
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/verify_delete_status")
    @ApiOperation(value = "Get the status when delete", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getVeryDeleteStatus(@PathParam("id") String id) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }
        final Object component = groupResource.getComponentById(id);
        if (component == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, Object> resultMap = new HashMap<>();
        final String canDelete = "canDelete";
        final String errorMessage = "errorMessage";

        resultMap.put(canDelete, true);

        try {
            if (component instanceof Label) {
                // enable to delete always
            } else if (component instanceof ProcessorNode) {
                serviceFacade.verifyDeleteProcessor(id);
            } else if (component instanceof Connection) {
                serviceFacade.verifyDeleteConnection(id);
            } else if (component instanceof Port) {
                Port port = (Port) component;
                if (port.getConnectableType().equals(ConnectableType.INPUT_PORT)) {
                    serviceFacade.verifyDeleteInputPort(id);
                } else if (port.getConnectableType().equals(ConnectableType.OUTPUT_PORT)) {
                    serviceFacade.verifyDeleteOutputPort(id);
                }
            } else if (component instanceof Funnel) {
                serviceFacade.verifyDeleteFunnel(id);
            } else if (component instanceof ControllerService) {
                serviceFacade.verifyDeleteControllerService(id);
            } else if (component instanceof ReportingTaskNode) {
                serviceFacade.verifyDeleteReportingTask(id);
            } else if (component instanceof ProcessGroup) {
                try {
                    serviceFacade.verifyDeleteProcessGroup(id);
                } catch (Exception e) {
                    Set<String> runningComponents = new HashSet<>();
                    Set<String> runningServices = new HashSet<>();
                    Set<String> queueConnections = new HashSet<>();
                    Set<String> holdingConnections = new HashSet<>();
                    collectGroupDetails(true, (ProcessGroup) component, runningComponents, runningServices, queueConnections, holdingConnections);
                    resultMap.put("runningComponents", runningComponents);
                    resultMap.put("runningServices", runningServices);
                    resultMap.put("queueConnections", queueConnections);
                    resultMap.put("holdingConnections", holdingConnections);
                    throw e;
                }
            } else if (component instanceof Snippet) {
                serviceFacade.verifyDeleteSnippet(id, serviceFacade.getRevisionsFromSnippet(id).stream().map(revision -> revision.getComponentId()).collect(Collectors.toSet()));
            }
        } catch (Exception e) {
            resultMap.put(canDelete, false);
            resultMap.put(errorMessage, e.getMessage());
        }

        return Response.ok().entity(resultMap).build();
    }

    /**
     * Copy the specified application.
     *
     * @param httpServletRequest
     *            request
     * @param requestAppCopyEntity
     *            The copy snippet request
     * @return A flowSnippetEntity.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/copy")
    @ApiOperation(value = "Copy a application.", response = ProcessGroupEntity.class, authorizations = { @Authorization(value = "Write - /process-groups/{uuid}"), //
            @Authorization(value = "Read - /{component-type}/{uuid} - For each component in the snippet and their descendant components"), //
            @Authorization(value = "Write - if the snippet contains any restricted Processors - /restricted-components") }) //
    public Response copyApp(//
            @Context HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The application copy request.", required = true) AppCopyEntity requestAppCopyEntity//
    ) {
        if (requestAppCopyEntity == null || requestAppCopyEntity.getAppCopy() == null) {
            throw new IllegalArgumentException("Application details must be specified.");
        }

        final AppCopyDTO requestAppCopyDTO = requestAppCopyEntity.getAppCopy();

        if (requestAppCopyDTO.getAppId() == null) {
            throw new IllegalArgumentException("Source application ID must be specified.");
        }

        final PositionDTO proposedPosition = requestAppCopyDTO.getPosition();
        if (proposedPosition != null) {
            if (proposedPosition.getX() == null || proposedPosition.getY() == null) {
                throw new IllegalArgumentException("The x and y coordinate of the proposed position must be specified.");
            }
        } else { // if not set the position, find new one
            final PositionDTO availablePosition = PositionCalcUtil.convert(PositionCalcUtil.newAvailablePosition(flowController));
            requestAppCopyDTO.setPosition(availablePosition);
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, requestAppCopyEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestAppCopyEntity.isDisconnectedNodeAcknowledged());
        }

        final String rootId = flowController.getRootGroupId();

        final SnippetDTO snippetDTO = new SnippetDTO();
        snippetDTO.setId(generateUuid());
        snippetDTO.setParentGroupId(rootId);
        final Map<String, RevisionDTO> app = new HashMap<>();
        app.put(requestAppCopyDTO.getAppId(), serviceFacade.getProcessGroup(requestAppCopyDTO.getAppId()).getRevision());
        snippetDTO.setProcessGroups(app);

        final ProcessGroupDTO processGroupDTO = new ProcessGroupDTO();
        processGroupDTO.setId(requestAppCopyDTO.getAppId());
        processGroupDTO.setName(requestAppCopyDTO.getName());
        processGroupDTO.setComments(requestAppCopyDTO.getComments());
        processGroupDTO.setTags(requestAppCopyDTO.getTags());
        processGroupDTO.setPosition(requestAppCopyDTO.getPosition());

        // get the revision from this snippet
        return withWriteLock(//
                serviceFacade, //
                requestAppCopyEntity, //
                lookup -> {
                    // ensure write access to the root process group
                    lookup.getProcessGroup("root").getAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // ensure write permission to every component in the snippet including referenced services
                    final SnippetAuthorizable snippet = lookup.getSnippet(snippetDTO);
                    authorizeSnippet(snippet, authorizer, lookup, RequestAction.WRITE, true, false);
                }, //
                null, //
                appCopyEntity -> {
                    final ProcessGroupEntity entity = serviceFacade.copyProcessGroup(rootId, snippetDTO, processGroupDTO, getIdGenerationSeed().orElse(null));
                    // generate the response
                    return generateCreatedResponse(getAbsolutePath(), entity).build();
                });
    }

    private void collectGroupDetails(Boolean isBegin, ProcessGroup group, Set<String> runningComponents, Set<String> runningServices, Set<String> queueConnections, Set<String> holdingConnections) {
        for (ProcessorNode processorNode : group.getProcessors()) {
            if (processorNode.getScheduledState().equals(ScheduledState.RUNNING)) {
                runningComponents.add(processorNode.getIdentifier());
            }
        }

        for (Connection connection : group.getConnections()) {
            if (!connection.getFlowFileQueue().isEmpty()) {
                queueConnections.add(connection.getIdentifier());
            }
        }

        for (ControllerServiceNode serviceNode : group.getControllerServices(false)) {
            if (serviceNode.getState().equals(ControllerServiceState.ENABLED)) {
                runningServices.add(serviceNode.getIdentifier());
            }
        }

        if (isBegin) {
            // 查询有没有connection连接到当前Group ，
            // 会将当前group有前置连接的ID 及 有后置连接且连接队列数据不为空的连接ID 放置到holdingConnections内
            for (Port inputPort : group.getInputPorts()) {
                for (Connection connection : inputPort.getIncomingConnections()) {
                    if (!connection.getProcessGroup().equals(group) && connection.getDestination().equals(inputPort)) {
                        holdingConnections.add(connection.getIdentifier());
                    }
                }
            }

            for (Port outputPort : group.getOutputPorts()) {
                for (Connection connection : outputPort.getConnections()) {
                    if (!connection.getProcessGroup().equals(group) && connection.getSource().equals(outputPort) && !connection.getFlowFileQueue().isEmpty()) {
                        holdingConnections.add(connection.getIdentifier());
                    }
                }
            }

            for (Funnel funnel : group.getFunnels()) {
                for (Connection connection : funnel.getIncomingConnections()) {
                    if (!connection.getProcessGroup().equals(group) && connection.getDestination().equals(funnel)) {
                        holdingConnections.add(connection.getIdentifier());
                    }
                }

                for (Connection connection : funnel.getConnections()) {
                    if (!connection.getProcessGroup().equals(group) && connection.getSource().equals(funnel) && !connection.getFlowFileQueue().isEmpty()) {
                        holdingConnections.add(connection.getIdentifier());
                    }
                }
            }

        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            collectGroupDetails(false, childGroup, runningComponents, runningServices, queueConnections, holdingConnections);
        }
    }

}
