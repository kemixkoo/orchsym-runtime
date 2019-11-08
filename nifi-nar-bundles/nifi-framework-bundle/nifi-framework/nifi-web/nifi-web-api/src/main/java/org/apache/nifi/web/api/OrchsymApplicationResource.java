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

import io.swagger.annotations.*;
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
import org.apache.nifi.web.api.entity.*;
import org.apache.nifi.web.revision.RevisionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @Autowired
    private FlowService flowService;

    @Autowired
    private RevisionManager revisionManager;

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
    @ApiOperation(value = "Performs a search against this NiFi using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
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
                if ("name".equalsIgnoreCase(sortedField)){
                    return isDesc? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                }else if ("createdTime".equalsIgnoreCase(sortedField)){
                    if (o1.getCreatedTime() != null && o2.getCreatedTime()!= null){
                        return isDesc? o2.getCreatedTime().compareTo(o1.getCreatedTime()): o1.getCreatedTime().compareTo(o2.getCreatedTime());
                    }else {
                        return isDesc? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                    }
                }else if ("modifiedTime".equalsIgnoreCase(sortedField)){
                    if (o1.getModifiedTime()!= null && o2.getModifiedTime()!= null){
                        return isDesc? o2.getModifiedTime().compareTo(o1.getModifiedTime()) : o1.getModifiedTime().compareTo(o2.getModifiedTime());
                    }else {
                        return isDesc? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                    }
                }else {
                    return isDesc? o2.getId().compareTo(o1.getId()) : o1.getId().compareTo(o2.getId());
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
    @Path("/component/{appId}/search-results")
    @ApiOperation(value = "Performs a search against this NiFi using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
    public Response searchFlowByGroup(
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String value,
            @ApiParam(value = "The group id", required = true)
            @PathParam("appId") @DefaultValue(StringUtils.EMPTY)
            final String appId
    ) throws InterruptedException {

        // query the controller
        final SearchResultsDTO results = serviceFacade.searchController(value, appId);

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
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response checkAppName(
            @QueryParam("name")String name,
            @QueryParam("appId") @DefaultValue(StringUtils.EMPTY) String appId
    ) {
        final boolean isAppNameValid = checkAppNameValid(name,appId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name",name);
        resultMap.put("isValid", isAppNameValid);
        return noCache(Response.ok(resultMap)).build();
    }

    /**
     * 检查新的APP名称是否合法。重名校验
     * @param appName
     * @return
     */
    protected boolean checkAppNameValid(final String appName, final String appId) {
        if (StringUtils.isBlank(appName)) {
            return false;
        }

        return !flowController.getRootGroup().getProcessGroups().stream() //
                .filter(g -> StringUtils.isBlank(appId) || !g.getIdentifier().equals(appId)) // exclude
                .filter(p -> p.getName().equals(appName))// existed
                .findFirst() //
                .isPresent();//

    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/status")
    @ApiOperation(value = "Get the status of current app", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getAppStatus(
            @PathParam("appId") @DefaultValue(StringUtils.EMPTY) String appId
    ) {
        final ProcessGroup groupApp = flowController.getGroup(appId);
        if (groupApp == null){
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        Map<String,Integer> countMap = new HashMap<>();
        countMap.put(runCount, 0);
        countMap.put(stoppedCount, 0);
        AtomicReference<Boolean> isStopped = new AtomicReference<>(false);
        setStatusCountOfAppByDirectName(appId,isStopped,countMap);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("id", appId);
        resultMap.put("canRun", countMap.get(stoppedCount) > 0);
        resultMap.put("canStop", countMap.get(runCount) > 0 );

        //  default value is can Disable 默认是已启用状态 可以禁用
        Boolean canEnable = false;
        Boolean canDisable = true;

        final String isEnabledStr = groupApp.getAddition(IS_ENABLED);
        if (isEnabledStr == null){
            final Map<String, String> additions = groupApp.getAdditions();
            Map<String,String> putMap = new HashMap<>();
            putMap.putAll(additions);
            putMap.put(IS_ENABLED, Boolean.toString(true));
            groupApp.setAdditions(putMap);
            flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
        }else {
            final boolean isEnabled = Boolean.parseBoolean(isEnabledStr);
            canEnable = !isEnabled;
            canDisable = isEnabled;
        }

        resultMap.put("canEnable",canEnable);
        resultMap.put("canDisable",canDisable);


        return noCache(Response.ok(resultMap)).build();
    }


    private static final String runCount = "runCount";
    private static final String stoppedCount = "stoppedCount";
    private static final String IS_ENABLED = "IS_ENABLED";
    private void setStatusCountOfAppByDirectName(String groupId, AtomicReference<Boolean> isStopped, Map<String,Integer> countMap){

        if (isStopped.get()){
            return;
        }
        final ProcessGroup group = flowController.getGroup(groupId);
        if (group == null){
            return;
        }

        // 一旦识别所有状态，立即返回
        if (countMap.get(runCount) != 0 && countMap.get(stoppedCount)!= 0){
            isStopped.set(true);
            return;
        }

        for (final ProcessorNode processor : group.getProcessors()) {
            final ScheduledState state = processor.getScheduledState();
            if (state.equals(ScheduledState.RUNNING)){
                countMap.put(runCount, countMap.get(runCount) + 1);
            }else if (state.equals(ScheduledState.STOPPED)){
                countMap.put(stoppedCount,countMap.get(stoppedCount) +1);
            }
        }

        for (final ControllerServiceNode service : group.getControllerServices(false)){
            final ControllerServiceState state = service.getState();
            if (state.equals(ControllerServiceState.ENABLED)){
                countMap.put(runCount, countMap.get(runCount) + 1);
            }else if (state.equals(ControllerServiceState.DISABLED)){
                countMap.put(stoppedCount, countMap.get(stoppedCount) +1);
            }
        }

        for(ProcessGroup childGroup : group.getProcessGroups()){
            setStatusCountOfAppByDirectName(childGroup.getIdentifier(), isStopped, countMap);
        }

    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/logic_delete")
    @ApiOperation(value = "delete the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response loginDeleteApp(
            @PathParam("appId") @DefaultValue(StringUtils.EMPTY) String appId
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }

        final ProcessGroup groupApp = flowController.getGroup(appId);
        if (groupApp == null){
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        if (!isCanLoginDeleteAppGroup(groupApp)){
            return Response.status(Response.Status.BAD_REQUEST).entity("the app must be one level group and haven't be deleted").build();
        }

        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(groupApp.getIdentifier());

        return withWriteLock(
                serviceFacade,
                groupEntity,
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(appId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
                },
                null,
                (entity) -> {
                    final ProcessGroup group = flowController.getGroup(entity.getId());
                    deleteGroupLogic(group);
                    return generateOkResponse("success").build();
                }
        );
    }

    private static final String IS_DELETED = "IS_DELETED";
    private Boolean  isCanLoginDeleteAppGroup(ProcessGroup group){
        if (!group.getParent().isRootGroup()){
            return false;
        }
        final String is_deleteStr = group.getAddition(IS_DELETED);
        if (Boolean.parseBoolean(is_deleteStr)){
            return false;
        }
        return true;
    }

    private void deleteGroupLogic(ProcessGroup group){
        for (ProcessorNode processorNode : group.getProcessors()){
            final ProcessorDTO processorDTO = new ProcessorDTO();
            processorDTO.setId(processorNode.getIdentifier());
            processorDTO.setState(ScheduleComponentsEntity.STATE_STOPPED);
            Revision revision = revisionManager.getRevision(processorNode.getIdentifier());
            serviceFacade.updateProcessor(revision, processorDTO);
        }

        for (Connection connection : group.getConnections()){
            DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(connection.getIdentifier(), generateUuid());
            serviceFacade.deleteFlowFileDropRequest(connection.getIdentifier(), dropRequest.getId());
        }

        for (ControllerServiceNode controllerServiceNode : group.getControllerServices(false)){
            Revision revision = revisionManager.getRevision(controllerServiceNode.getIdentifier());
            ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
            controllerServiceDTO.setId(controllerServiceNode.getIdentifier());
            controllerServiceDTO.setState(ScheduleComponentsEntity.STATE_DISABLED);
            serviceFacade.updateControllerService(revision,controllerServiceDTO);
        }

        for (ProcessGroup childGroup : group.getProcessGroups()){
            deleteGroupLogic(childGroup);
        }

        // finally update status of group
        if (group.getParent().isRootGroup()){
            final Map<String, String> additions = group.getAdditions();
            Map<String, String> putMap = new HashMap<>();
            putMap.putAll(additions);
            putMap.put(IS_DELETED, Boolean.toString(true));
            group.setAdditions(putMap);
            flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
        }
    }


    @PUT
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/{appId}/status")
    @ApiOperation(value = "delete the app logically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response enableOrDisableOrRecoverApp(
            @Context HttpServletRequest httpServletRequest,
            @PathParam("appId") @DefaultValue(StringUtils.EMPTY) String appId,
           final AppStatusEntity appStatusEntity

    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, appStatusEntity);
        }

        final ProcessGroup groupApp = flowController.getGroup(appId);
        if (groupApp == null){
            return Response.status(Response.Status.NOT_FOUND).entity("cant find the group by the appId").build();
        }

        final Boolean isEnabled = appStatusEntity.getEnabled();
        final Boolean isRecover = appStatusEntity.getRecover();

        ProcessGroupEntity groupEntity = new ProcessGroupEntity();
        groupEntity.setId(groupApp.getIdentifier());

        return withWriteLock(
                serviceFacade,
                groupEntity,
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(appId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
                },
                null,
                (entity) -> {
                    final ProcessGroup group = flowController.getGroup(entity.getId());
                    if (isEnabled != null || isRecover != null){
                        final Map<String, String> additions = group.getAdditions();
                        Map<String, String> putMap = new HashMap<>();
                        putMap.putAll(additions);
                        if (isEnabled != null){
                            putMap.put(IS_ENABLED, String.valueOf(isEnabled));
                        }
                        if (isRecover != null && isRecover){
                            putMap.put(IS_DELETED, String.valueOf(false));
                        }
                        group.setAdditions(putMap);
                        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);
                    }
                    return generateOkResponse("success").build();
                }
        );


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
            @PathParam("groupId") @DefaultValue(StringUtils.EMPTY) String groupId
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
    @Path("/group/{groupId}/physics_delete")
    @ApiOperation(value = "delete the app or group physically", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response physicsDeleteApp(
            @PathParam("groupId") @DefaultValue(StringUtils.EMPTY) String groupId
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
