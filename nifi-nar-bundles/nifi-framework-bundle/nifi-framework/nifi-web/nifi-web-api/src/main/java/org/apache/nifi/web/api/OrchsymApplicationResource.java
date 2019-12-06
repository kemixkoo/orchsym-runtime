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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.controller.Snippet;
import org.apache.nifi.controller.label.Label;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.util.PositionCalcUtil;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.SnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.OrchsymCreateTemplateReqEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.SearchResultsEntity;
import org.apache.nifi.web.api.entity.SnippetEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.orchsym.DataPage;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.api.orchsym.application.AppCopyEntity;
import org.apache.nifi.web.api.orchsym.application.AppGroupEntity;
import org.apache.nifi.web.api.orchsym.application.AppSearchEntity;
import org.apache.nifi.web.api.orchsym.application.ApplicationFieldName;
import org.apache.nifi.web.api.orchsym.template.TemplateFieldName;
import org.apache.nifi.web.revision.RevisionManager;
import org.apache.nifi.web.util.AppTypeAssessor;
import org.apache.nifi.web.util.AppTypeAssessor.AppType;
import org.apache.nifi.web.util.ChinesePinyinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Autowired
    private FlowService flowService;

    @Autowired
    private OrchsymGroupResource orchsymGroupResource;

    @Autowired
    private ProcessGroupResource groupResource;

    @Autowired
    private TemplateResource templateResource;

    @Autowired
    private RevisionManager revisionManager;

    private Response verifyApp(String appId) {
        boolean existed = flowController.getRootGroup().getProcessGroups().stream().filter(group -> group.getIdentifier().equals(appId)).findAny().isPresent();
        if (!existed) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }
        return null;
    }

    /**
     * 
     * 应用下载为模板的数据生成
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/template/{appId}/data")
    @ApiOperation(value = "Get the template data of current app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAppTemplateData(//
            @PathParam("appId") String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        return orchsymGroupResource.generateTemplateData(appId);
    }

    /**
     * 将应用保存为模板
     */
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/template/{appId}/saveas")
    @ApiOperation(value = "create template by app group", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response generateTemplateByApp(//
            @PathParam("appId") String appId, //
            final OrchsymCreateTemplateReqEntity templateReqEntity//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (templateReqEntity.getCreatedTime() == null) {
            templateReqEntity.setCreatedTime(System.currentTimeMillis());
        }
        if (templateReqEntity.getCreatedUser() == null) {
            templateReqEntity.setCreatedUser(NiFiUserUtils.getNiFiUserIdentity());
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, templateReqEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(templateReqEntity.isDisconnectedNodeAcknowledged());
        }
        final ProcessGroup appGroup = flowController.getGroup(appId);
        final String rootGroupId = flowController.getRootGroupId();

        // 如果未提供，则直接重用应用相关信息
        final String templateName = Objects.isNull(templateReqEntity.getName()) ? appGroup.getName() : templateReqEntity.getName();
        final String templateDesc = Objects.isNull(templateReqEntity.getDescription()) ? appGroup.getComments() : templateReqEntity.getDescription();
        final Set<String> templateTags = Objects.isNull(templateReqEntity.getTags()) ? appGroup.getTags() : templateReqEntity.getTags();

        return withWriteLock(//
                serviceFacade, //
                templateReqEntity, //
                lookup -> {
                    final Authorizable authorizable = lookup.getProcessGroup(appId).getAuthorizable();
                    authorizable.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
                }, //
                () -> {
                    if (!templateReqEntity.isOverwrite()) {
                        serviceFacade.verifyCanAddTemplate(rootGroupId, templateName);
                    }
                }, //
                createTemplateRequestEntity -> {
                    if (createTemplateRequestEntity.isOverwrite()) {
                        serviceFacade.getTemplates().stream()//
                                .map(TemplateEntity::getTemplate)//
                                .filter(dto -> templateName.equals(dto.getName()))//
                                .collect(Collectors.toSet())//
                                .forEach(dto -> {
                                    serviceFacade.deleteTemplate(dto.getId());
                                });
                    }

                    Revision revision = revisionManager.getRevision(appId);
                    SnippetDTO snippetDTO = new SnippetDTO();
                    Map<String, RevisionDTO> revisionMap = new HashMap<>();
                    RevisionDTO revisionDTO = new RevisionDTO();
                    revisionDTO.setClientId(revision.getClientId());
                    revisionDTO.setVersion(revision.getVersion());
                    revisionMap.put(appId, revisionDTO);
                    snippetDTO.setProcessGroups(revisionMap);
                    snippetDTO.setId(generateUuid());
                    snippetDTO.setParentGroupId(rootGroupId);
                    final SnippetEntity snippetEntity = serviceFacade.createSnippet(snippetDTO);
                    final String snippetId = snippetEntity.getSnippet().getId();

                    final Map<String, String> templateAdditions = TemplateFieldName.getCreatedAdditions(createTemplateRequestEntity, true, NiFiUserUtils.getNiFiUserIdentity());

                    TemplateDTO template = serviceFacade.createTemplate(templateAdditions, templateTags, templateName, templateDesc, snippetId, rootGroupId, getIdGenerationSeed());
                    templateResource.populateRemainingTemplateContent(template);
                    final TemplateEntity entity = new TemplateEntity();
                    entity.setTemplate(template);

                    // build the response
                    return generateCreatedResponse(URI.create(template.getUri()), entity).build();
                });
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
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchApp(//
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String text, //

            //
            @QueryParam("page") @DefaultValue("1") int currentPage, //
            @QueryParam("pageSize") @DefaultValue("10") int pageSize, //

            // sort
            @QueryParam("sortedField") @DefaultValue("name") String sortedField, // 默认以名字排序
            @QueryParam("isDesc") @DefaultValue("true") boolean isDesc, // 默认降序

            // filter
            @QueryParam("isDeleted") @DefaultValue("false") boolean isDeleted, // 默认为非删除
            @QueryParam("isEnabled") Boolean isEnabled, // 默认忽略该标记，所以允许null
            @QueryParam("isRunning") Boolean isRunning, // 默认忽略该标记，所以允许null
            @QueryParam("hasDataQueue") Boolean hasDataQueue, // 默认忽略该标记，所以允许null
            @QueryParam("timeField") String timeField, //
            @QueryParam("beginTime") Long beginTime, //
            @QueryParam("endTime") Long endTime, //
            @QueryParam("tags") String tags, // 英文逗号分隔多个

            //
            @QueryParam("isDetail") @DefaultValue("false") Boolean needDetail //

    ) throws InterruptedException {
        AppSearchEntity searchEnity = new AppSearchEntity();
        searchEnity.setText(text);
        searchEnity.setPage(currentPage);
        searchEnity.setPageSize(pageSize);
        searchEnity.setSortedField(sortedField);
        searchEnity.setDesc(isDesc);
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

    private static final Function<? super String, ? extends String> FUN_LOWERCASE = t -> t.toLowerCase();

    private void fixDefaultSearchEnity(final AppSearchEntity searchEnity) {

        // FIXME, 暂不支持modified_time，否应该删除统一到创建日期上
        if (AppSearchEntity.PARAM_MODIFIED_TIME.equalsIgnoreCase(searchEnity.getSortedField())) {
            searchEnity.setSortedField(AppSearchEntity.PARAM_CREATED_TIME);
        }
        if (AppSearchEntity.PARAM_MODIFIED_TIME.equalsIgnoreCase(searchEnity.getFilterTimeField())) {
            searchEnity.setFilterTimeField(AppSearchEntity.PARAM_CREATED_TIME);
        }

    }

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/search-results")
    @ApiOperation(value = "Performs a search against this runtime using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    public Response searchApp(//
            @RequestBody final AppSearchEntity searchEnity//
    ) {
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

                        groupEntity.setCreatedTime(ProcessUtil.getGroupAdditionLongValue(group, AdditionConstants.KEY_CREATED_TIMESTAMP));
                        groupEntity.setModifiedTime(ProcessUtil.getGroupAdditionLongValue(group, AdditionConstants.KEY_MODIFIED_TIMESTAMP));

                        Boolean deleted = ProcessUtil.getGroupAdditionBooleanValue(group, AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT);
                        if (Objects.isNull(deleted)) {
                            deleted = AdditionConstants.KEY_IS_DELETED_DEFAULT;
                        }
                        groupEntity.setDeleted(deleted);
                        Boolean enabled = ProcessUtil.getGroupAdditionBooleanValue(group, AdditionConstants.KEY_IS_ENABLED, AdditionConstants.KEY_IS_ENABLED_DEFAULT);
                        if (Objects.isNull(deleted)) {
                            enabled = AdditionConstants.KEY_IS_ENABLED_DEFAULT;
                        }
                        groupEntity.setEnabled(enabled);

                        if (null != group.getTags()) {
                            groupEntity.setTags(new HashSet<>(group.getTags()));
                        }
                        final AppType appType = AppTypeAssessor.judgeType(group);
                        groupEntity.setType(appType.getName());
                    }

                    return groupEntity;
                }).collect(Collectors.toList());

        // 进行筛选
        final boolean deleted = searchEnity.isDeleted();
        final Boolean enabled = searchEnity.getEnabled();
        final Boolean isRunning = searchEnity.getIsRunning();
        final Boolean hasDataQueue = searchEnity.getHasDataQueue();
        final Set<String> tags = searchEnity.getTags();
        final String filterTimeField = searchEnity.getFilterTimeField();
        final Long beginTime = searchEnity.getBeginTime();
        final Long endTime = searchEnity.getEndTime();

        appGroupEntityList = appGroupEntityList.stream().filter(appGroupEntity -> {
            if (deleted != appGroupEntity.isDeleted()) {
                return false;
            }
            if (!Objects.isNull(enabled) // 设置值
                    && enabled != appGroupEntity.isEnabled()) {
                return false;
            }
            if (!Objects.isNull(isRunning)) {// 设置值
                final ProcessGroupEntity groupEntity = serviceFacade.getProcessGroup(appGroupEntity.getId());
                if (!isRunning.equals((groupEntity.getRunningCount() > 0))) {
                    return false;
                }
            }
            if (!Objects.isNull(hasDataQueue) // 设置值
                    && !hasDataQueue.equals(isGroupHasDataQueue(appGroupEntity.getId()))) {
                return false;
            }
            if (!Objects.isNull(tags)) {
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
                if (AppSearchEntity.PARAM_CREATED_TIME.equals(filterTimeField)) {
                    final Long createdTime = appGroupEntity.getCreatedTime();
                    if (beginTime != null && createdTime < beginTime) {
                        return false;
                    }
                    if (endTime != null && createdTime > endTime) {
                        return false;
                    }
                } else if (AppSearchEntity.PARAM_MODIFIED_TIME.equals(filterTimeField)) {
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
        final boolean isDesc = searchEnity.isDesc();
        final Comparator<AppGroupEntity> nameComparator = new Comparator<AppGroupEntity>() {
            @Override
            public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                final int compare = ChinesePinyinUtil.zhComparator.compare(o2.getName(), o1.getName());
                return isDesc ? compare : -compare;
            }
        };

        Predicate<? super AppGroupEntity> timePredicate = null;
        Comparator<AppGroupEntity> timeComparator = null;
        if (!Objects.isNull(sortField)) {
            if (AppSearchEntity.PARAM_CREATED_TIME.equalsIgnoreCase(sortField)) {
                timePredicate = app -> !Objects.isNull(app.getCreatedTime());
                timeComparator = new Comparator<AppGroupEntity>() {
                    @Override
                    public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                        final int compare = o2.getCreatedTime().compareTo(o1.getCreatedTime());
                        return isDesc ? compare : -compare;
                    }
                };

            } else if (AppSearchEntity.PARAM_MODIFIED_TIME.equalsIgnoreCase(sortField)) {
                timePredicate = app -> !Objects.isNull(app.getModifiedTime());
                timeComparator = new Comparator<AppGroupEntity>() {
                    @Override
                    public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                        final int compare = o2.getModifiedTime().compareTo(o1.getModifiedTime());
                        return isDesc ? compare : -compare;
                    }
                };
            } // 名字排序
        }

        if (!Objects.isNull(timePredicate) && !Objects.isNull(timeComparator)) {
            // 需分桶处理不存在日期的情况
            List<AppGroupEntity> holdTimeList = appGroupEntityList.stream().filter(timePredicate).sorted(timeComparator).collect(Collectors.toList());

            // 没有名字的，按名字排序
            List<AppGroupEntity> noTimeList = appGroupEntityList.stream().filter(timePredicate.negate()).sorted(nameComparator).collect(Collectors.toList());

            appGroupEntityList.clear();
            appGroupEntityList.addAll(holdTimeList);
            appGroupEntityList.addAll(noTimeList);// 并放于最后
        } else { // 名字排序
            Collections.sort(appGroupEntityList, nameComparator);
        }

        DataPage<AppGroupEntity> appsPage = new DataPage<AppGroupEntity>(appGroupEntityList, searchEnity.getPageSize(), searchEnity.getPage());
        if (searchEnity.isNeedDetail()) {
            DataPage<ProcessGroupEntity> detailsPage = new DataPage<ProcessGroupEntity>();
            // 设置返回信息同原appsPage的分页信息，并保持一致
            detailsPage.setCurrentPage(appsPage.getCurrentPage());
            detailsPage.setPageSize(appsPage.getPageSize());
            detailsPage.setTotalPage(appsPage.getTotalPage());
            detailsPage.setTotalSize(appsPage.getTotalSize());

            // 仅更新结果集
            List<ProcessGroupEntity> detailsList = appsPage.getResults().stream().map(app -> serviceFacade.getProcessGroup(app.getId())).collect(Collectors.toList());
            detailsPage.setResults(detailsList);

            return noCache(Response.ok(detailsPage)).build();
        } else {
            return noCache(Response.ok(appsPage)).build();
        }
    }

    private boolean isGroupHasDataQueue(String groupId) {
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

    /**
     * @param name
     *            编辑或创建时的应用新命名
     * @param appId
     *            编辑时，当前应用id
     * @return
     *         在创建时，一般为空；
     *         在编辑时，为了验证重命名，需要排除自己，所以需要提供当前应用id
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/name/valid")
    @ApiOperation(value = "check the name of current app", //
            response = Map.class)
    public Response isAppNewNameValid(//
            @QueryParam("name") String name, //
            @QueryParam("appId") String appId//
    ) {
        final boolean validName = validName(name, appId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("isValid", validName);
        return noCache(Response.ok(resultMap)).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/check_name")
    @ApiOperation(value = "check the name of current app", //
            response = Map.class)
    public Response checkAppName(//
            @QueryParam("name") String name, //
            @QueryParam("appId") String appId//
    ) {
        return isAppNewNameValid(name, appId);
    }

    private boolean validName(final String newName, final String curAppId) {
        boolean isAppNameValid = true;
        if (StringUtils.isBlank(newName)) { // 不能为空
            isAppNameValid = false;
        } else {
            isAppNameValid = !flowController.getRootGroup().getProcessGroups().stream() //
                    .filter(g -> StringUtils.isBlank(curAppId) // 新建时为空
                            || !g.getIdentifier().equals(curAppId)) // 改名时，排除当前应用
                    .filter(p -> p.getName().equals(newName))// existed
                    .findFirst() //
                    .isPresent();//
        }
        return isAppNameValid;
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/status")
    @ApiOperation(value = "Get the status of current app", //
            response = Map.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAppStatus(//
            @PathParam("appId") final String appId//
    ) {
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
        boolean isEnabled = ProcessUtil.getGroupAdditionBooleanValue(groupApp, AdditionConstants.KEY_IS_ENABLED, AdditionConstants.KEY_IS_ENABLED_DEFAULT);
        resultMap.put("canEnable", !isEnabled);
        resultMap.put("canDisable", isEnabled);

        boolean isDeleted = ProcessUtil.getGroupAdditionBooleanValue(groupApp, AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT);
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
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/logic_delete")
    @ApiOperation(value = "delete the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response logicDeleteApp(//
            @PathParam("appId") final String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }
        Consumer<String> cleanAction = (id) -> {
            final ProcessGroup group = flowController.getGroup(id);
            // 停组件和服务，清队列，保留模板
            orchsymGroupResource.safeCleanGroup(group, true, true, true, false);
        };

        return updateAppStatus(appId, AdditionConstants.KEY_IS_DELETED, Boolean.TRUE, cleanAction);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/logic_delete_by_name")
    @ApiOperation(value = "delete the app via name logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response logicDeleteApp(//
            @RequestBody final AppGroupEntity appGroupEntity//
    ) {
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
        Consumer<String> cleanAction = (id) -> {
            final ProcessGroup group = flowController.getGroup(id);
            // 停组件和服务，清队列，保留模板
            orchsymGroupResource.safeCleanGroup(group, true, true, true, false);
        };

        return updateAppStatus(findFirst.get().getIdentifier(), AdditionConstants.KEY_IS_DELETED, Boolean.TRUE, cleanAction);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/recover")
    @ApiOperation(value = "recover the app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response recoverApp(//
            @Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        }

        return updateAppStatus(appId, AdditionConstants.KEY_IS_DELETED, Boolean.FALSE, null);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/enable")
    @ApiOperation(value = "enable the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response enableApp(//
            @Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        }

        return updateAppStatus(appId, AdditionConstants.KEY_IS_ENABLED, Boolean.TRUE, null);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/disable")
    @ApiOperation(value = "disable the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response disableApp(//
            @Context HttpServletRequest httpServletRequest, //
            @PathParam("appId") String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        }
        Consumer<String> cleanAction = (id) -> {
            final ProcessGroup group = flowController.getGroup(id);
            // 停组件和服务，保留队列和模板
            orchsymGroupResource.safeCleanGroup(group, true, true, false, false);
        };
        return updateAppStatus(appId, AdditionConstants.KEY_IS_ENABLED, Boolean.FALSE, cleanAction);
    }

    private Response updateAppStatus(final String appId, final String additionKey, final Object value, final Consumer<String> cleanAction) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(appId);

        return withWriteLock(//
                serviceFacade, //
                groupEntity, //
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(appId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                }, //
                null, //
                (entity) -> {
                    if (!Objects.isNull(cleanAction)) {
                        cleanAction.accept(entity.getId());
                    }
                    saveAppStatus(entity.getId(), additionKey, value);
                    return generateOkResponse("success").build();
                });
    }

    private void saveAppStatus(String appId, String key, Object value) {
        final ProcessGroup group = flowController.getGroup(appId);

        group.getAdditions().setValue(key, value);

        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{appId}/force_delete")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404)//
    })
    public Response forceDeleteApp(//
            @PathParam("appId") String appId//
    ) {
        final Response verifyApp = verifyApp(appId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }
        final ProcessGroup groupApp = flowController.getGroup(appId);
        if (groupApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the application of the groupId: " + appId).build();
        }
        return orchsymGroupResource.getResponseForForceDeleteGroup(groupApp);
    }

    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/force_delete_by_name")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response forceDeleteAppByName(//
            @RequestBody final AppGroupEntity appGroupEntity //
    ) {
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

        return orchsymGroupResource.getResponseForForceDeleteGroup(findFirst.get());
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{id}/verify_delete_status")
    @ApiOperation(value = "Get the status when delete", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getVeryDeleteStatus(//
            @PathParam("id") String id //
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }
        final Object component = orchsymGroupResource.getComponentById(id);
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
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/copy")
    @ApiOperation(value = "Copy a application.", response = ProcessGroupEntity.class, authorizations = { @Authorization(value = "Write - /process-groups/{uuid}"), //
            @Authorization(value = "Read - /{component-type}/{uuid} - For each component in the snippet and their descendant components"), //
            @Authorization(value = "Write - if the snippet contains any restricted Processors - /restricted-components") }) //
    public Response copyApp(//
            @Context HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The application copy request.", required = true) AppCopyEntity requestAppCopyEntity//
    ) {
        final String sourceAppId = requestAppCopyEntity.getAppId();
        if (sourceAppId == null) {
            throw new IllegalArgumentException("Source application ID must be specified.");
        }
        final Response verifyApp = verifyApp(sourceAppId);
        if (null != verifyApp) {// has error
            return verifyApp;
        }

        if (null == requestAppCopyEntity.getCreatedTime()) {
            requestAppCopyEntity.setCreatedTime(System.currentTimeMillis());
        }
        final String user = NiFiUserUtils.getNiFiUserIdentity();
        if (null == requestAppCopyEntity.getCreatedUser()) {
            requestAppCopyEntity.setCreatedUser(user);
        }
        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, requestAppCopyEntity);
        }
        final String newName = requestAppCopyEntity.getName();
        if (!validName(newName, null)) {
            return Response.status(Response.Status.CONFLICT).entity(newName).build();
        }

        final String rootId = flowController.getRootGroupId();
        final ProcessGroupEntity sourceApp = serviceFacade.getProcessGroup(sourceAppId);

        final SnippetDTO snippetDTO = new SnippetDTO();
        snippetDTO.setId(generateUuid());
        snippetDTO.setParentGroupId(rootId);
        final Map<String, RevisionDTO> app = new HashMap<>();
        app.put(sourceAppId, sourceApp.getRevision());
        snippetDTO.setProcessGroups(app);

        final ProcessGroupDTO processGroupDTO = new ProcessGroupDTO();
        processGroupDTO.setId(sourceAppId);
        processGroupDTO.setName(newName);
        processGroupDTO.setComments(requestAppCopyEntity.getComments());
        processGroupDTO.setTags(requestAppCopyEntity.getTags());

        Map<String, String> additions = sourceApp.getComponent().getAdditions();
        if (null == additions) {
            additions = new HashMap<>();
        } else {
            additions = new HashMap<>(additions);
        }
        additions.putAll(ApplicationFieldName.getCreatingAdditions(additions, user));
        // 重新设置创建时间戳
        additions.put(AdditionConstants.KEY_CREATED_TIMESTAMP, requestAppCopyEntity.getCreatedTime().toString());
        additions.put(AdditionConstants.KEY_CREATED_USER, requestAppCopyEntity.getCreatedUser());
        processGroupDTO.setAdditions(additions);

        final PositionDTO availablePosition = PositionCalcUtil.convert(PositionCalcUtil.newAvailablePosition(flowController));
        processGroupDTO.setPosition(availablePosition);

        // get the revision from this snippet
        return withWriteLock(//
                serviceFacade, //
                requestAppCopyEntity, //
                lookup -> {
                    // ensure write access to the root process group
                    final Authorizable authorizable = lookup.getProcessGroup(FlowController.ROOT_GROUP_ID_ALIAS).getAuthorizable();
                    authorizable.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

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

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{id}/app-entity")
    @ApiOperation(value = "Get the status of current app", //
            response = Map.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAppEntity(//
            @PathParam("id") final String anyId//
    ) {

        // authorize access
        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable processGroup = lookup.getProcessGroup(anyId).getAuthorizable();
            processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
        });
        ProcessGroup appGroup = orchsymGroupResource.getAppGroup(anyId);
        if (Objects.isNull(appGroup)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // get this process group contents
        final ProcessGroupEntity appEntity = serviceFacade.getProcessGroup(appGroup.getIdentifier());
        groupResource.populateRemainingProcessGroupEntityContent(appEntity);

        if (appEntity.getComponent() != null) {
            appEntity.getComponent().setContents(null);
        }

        return generateOkResponse(appEntity).build();
    }
}
