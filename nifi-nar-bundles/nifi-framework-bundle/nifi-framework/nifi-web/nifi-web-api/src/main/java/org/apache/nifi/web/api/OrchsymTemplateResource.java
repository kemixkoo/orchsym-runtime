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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

import org.apache.commons.collections4.list.TreeList;
import org.apache.nifi.authorization.AuthorizableLookup;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.SnippetAuthorizable;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.Template;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.util.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.OrchsymCreateTemplateReqEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.orchsym.DataPage;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.api.orchsym.template.TemplateFavority;
import org.apache.nifi.web.api.orchsym.template.TemplateFiledName;
import org.apache.nifi.web.api.orchsym.template.TemplateSearchEntity;
import org.apache.nifi.web.api.orchsym.template.TemplateSourceType;
import org.apache.nifi.web.api.orchsym.template.TemplateType;
import org.apache.nifi.web.dao.TemplateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * @author liuxun
 * @apiNote 处理connections清除队列的相关功能
 */
@Component
@Path("/orchsym-template")
@Api(value = "/orchsym-template", description = "for Template")
public class OrchsymTemplateResource extends AbsOrchsymResource {
    @Autowired
    private TemplateResource templateResource;

    @Autowired
    private TemplateDAO templateDAO;

    @Autowired
    private FlowService flowService;

    /**
     * 新的创建模板的接口 (替换旧接口，兼容了老接口的参数)
     * 
     * @param groupId
     *            模板所在group的ID
     * @param requestCreateTemplateRequestEntity
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}")
    @ApiOperation(value = "Creates a template and discards the specified snippet.", response = TemplateEntity.class, authorizations = { @Authorization(value = "Write - /process-groups/{uuid}"),
            @Authorization(value = "Read - /{component-type}/{uuid} - For each component in the snippet and their descendant components") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 404, message = "The specified resource could not be found."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
    public Response createTemplate(@Context final HttpServletRequest httpServletRequest, @ApiParam(value = "The process group id.", required = true) @PathParam("groupId") final String groupId,
            @ApiParam(value = "The create template request.", required = true) final OrchsymCreateTemplateReqEntity requestCreateTemplateRequestEntity) {

        if (requestCreateTemplateRequestEntity.getSnippetId() == null) {
            throw new IllegalArgumentException("The snippet identifier must be specified.");
        }

        if (StringUtils.isBlank(requestCreateTemplateRequestEntity.getName())) {
            throw new IllegalArgumentException("The template name must be specified.");
        }

        if (requestCreateTemplateRequestEntity.getCreatedTime() == null) {
            requestCreateTemplateRequestEntity.setCreatedTime(System.currentTimeMillis());
        }

        if (requestCreateTemplateRequestEntity.getCreatedUser() == null) {
            requestCreateTemplateRequestEntity.setCreatedUser(NiFiUserUtils.getNiFiUserIdentity());
        }

        if (requestCreateTemplateRequestEntity.getSourceType() == null) {
            // 创建模板默认属于 另存类型
            requestCreateTemplateRequestEntity.setSourceType(TemplateSourceType.SAVE_AS.name());
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, requestCreateTemplateRequestEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestCreateTemplateRequestEntity.isDisconnectedNodeAcknowledged());
        }

        return withWriteLock(//
                serviceFacade, //
                requestCreateTemplateRequestEntity, //
                lookup -> {
                    authorizeSnippetUsage(lookup, groupId, requestCreateTemplateRequestEntity.getSnippetId(), true);
                }, //
                () -> {
                    if (!requestCreateTemplateRequestEntity.isOverwrite()) { // 如果不是覆盖，则验证名字，否则忽略，并将稍后删除存在同名的
                        serviceFacade.verifyCanAddTemplate(groupId, requestCreateTemplateRequestEntity.getName());
                    }
                }, //
                createTemplateRequestEntity -> {
                    final String newName = createTemplateRequestEntity.getName();
                    // 如果设置了overwrite为true，先进行重名模板的清理
                    if (createTemplateRequestEntity.isOverwrite()) {
                        serviceFacade.getTemplates().stream()//
                                .map(TemplateEntity::getTemplate)//
                                .filter(dto -> newName.equals(dto.getName()))//
                                .collect(Collectors.toSet())//
                                .forEach(dto -> {
                                    serviceFacade.deleteTemplate(dto.getId());
                                });
                    }

                    final Map<String, String> contentsMap = getContentsMapFromEntity(createTemplateRequestEntity);
                    final ProcessGroup group = flowController.getGroup(groupId);
                    if (group.getParent().isRootGroup()) {
                        contentsMap.put(TemplateFiledName.TEMPLATE_TYPE, TemplateType.APPLICATION.name());
                    } else {
                        contentsMap.put(TemplateFiledName.TEMPLATE_TYPE, TemplateType.NORMAL.name());
                    }

                    final Set<String> tagsSet = createTemplateRequestEntity.getTags();
                    // create the template and generate the json
                    final TemplateDTO template = serviceFacade.createTemplate(contentsMap, tagsSet, newName, createTemplateRequestEntity.getDescription(), createTemplateRequestEntity.getSnippetId(),
                            groupId, getIdGenerationSeed());
                    templateResource.populateRemainingTemplateContent(template);

                    // build the response entity
                    final TemplateEntity entity = new TemplateEntity();
                    entity.setTemplate(template);

                    // build the response
                    return generateCreatedResponse(URI.create(template.getUri()), entity).build();
                });
    }

    /**
     * 用于编辑指定模板的 名称，描述或 tags标签
     * 
     * @param tempParam
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/edit")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = "client error"), //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response editTemplate(final TemplateDTO tempParam) {

        if (tempParam == null || tempParam.getId() == null) {
            throw new IllegalArgumentException("The template cant be null and the identifier must be specified.");
        }

        // 验证 description name tags 至少有一项修改内容
        if (StringUtils.isBlank(tempParam.getName()) && StringUtils.isBlank(tempParam.getDescription()) && (tempParam.getTags() == null || tempParam.getTags().isEmpty())) {
            throw new IllegalArgumentException("the  modified contents of template cant be empty");
        }

        // 确定是否是转发的
        if (tempParam.getAdditions() == null
                || !(tempParam.getAdditions().containsKey(AdditionConstants.KEY_MODIFIED_USER) && tempParam.getAdditions().containsKey(AdditionConstants.KEY_MODIFIED_TIMESTAMP))) {
            Map<String, String> additionsParam = new HashMap<>();
            additionsParam.put(AdditionConstants.KEY_MODIFIED_USER, NiFiUserUtils.getNiFiUserIdentity());
            additionsParam.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, Long.toString(System.currentTimeMillis()));
            tempParam.setAdditions(new HashMap<>(additionsParam));
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT, tempParam);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.FORBIDDEN).entity("current node has been disconnected from cluster").build();
        }

        Template template = null;
        try {
            template = templateDAO.getTemplate(tempParam.getId());
        } catch (Exception ignored) {
            throw new IllegalArgumentException("The template not exists");
        }

        final TemplateDTO originTempDTO = template.getDetails();

        // 重名校验
        if (!StringUtils.isBlank(tempParam.getName()) && !verifyTempNameValid(tempParam.getName(), originTempDTO.getId())) {
            throw new IllegalArgumentException("The template name cant be duplicate");
        }

        final TemplateEntity requestTemplateEntity = new TemplateEntity();
        requestTemplateEntity.setId(tempParam.getId());

        return withWriteLock(serviceFacade, requestTemplateEntity, lookup -> {
            final Authorizable temp = lookup.getTemplate(tempParam.getId());

            // ensure write permission to the template
            temp.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

            // ensure write permission to the parent process group
            temp.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
        }, null, (templateEntity) -> {
            final Map<String, String> additions = originTempDTO.getAdditions() == null ? new HashMap<>() : originTempDTO.getAdditions();
            additions.put(AdditionConstants.KEY_MODIFIED_USER, tempParam.getAdditions().get(AdditionConstants.KEY_MODIFIED_USER));
            additions.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, tempParam.getAdditions().get(AdditionConstants.KEY_MODIFIED_USER));

            originTempDTO.setAdditions(additions);

            if (!StringUtils.isBlank(tempParam.getName())) {
                originTempDTO.setName(tempParam.getName());
            }

            if (!StringUtils.isBlank(tempParam.getDescription())) {
                originTempDTO.setDescription(tempParam.getDescription());
            }

            if (tempParam.getTags() != null && !tempParam.getTags().isEmpty()) {
                originTempDTO.setTags(tempParam.getTags());
            }

            // 保存持久化
            flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);

            return generateOkResponse(originTempDTO).build();
        });
    }

    /**
     * 验证template的新名称是否合法(不重复 不为空)
     *
     * @param name
     *            不允许为空
     * @param templateId
     *            如果为null 表示新建 ; 如果不为null 表示编辑修改
     * @return
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/name/valid")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response isTemplateNewNameValid(@QueryParam("name") String name, @QueryParam("templateId") String templateId) {
        if (StringUtils.isBlank(name)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("new name cant be empty").build();
        }

        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("isValid", verifyTempNameValid(name, templateId));

        return Response.ok(resultMap).build();
    }

    private static final Predicate<? super TemplateEntity> PRE_FILTER = t -> TemplateSourceType.OFFICIAL.not(t.getTemplate().getAdditions());

    /**
     * 对自定义类型的模板进行GET方式查询
     * 
     * @return
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/custom/search")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response searchCustomTempsByQueryParams(//
            @QueryParam("text") String text, //

            // page
            @QueryParam("page") @DefaultValue("1") Integer currentPage, //
            @QueryParam("pageSize") @DefaultValue("10") Integer pageSize, //

            // sort
            @QueryParam("sortedField") @DefaultValue("createdTime") String sortedField, //
            @QueryParam("isDesc") @DefaultValue("true") Boolean isDesc, //
            @QueryParam("deleted") @DefaultValue("false") boolean deleted, //

            @QueryParam("templateType") String templateType, //
            @QueryParam("sourceType") String sourceType, //

            // filter
            @QueryParam("filterTimeField") String filterTimeField, //
            @QueryParam("beginTime") Long beginTime, //
            @QueryParam("endTime") Long endTime, //
            @QueryParam("tags") String tags // 英文逗号分隔多个
    ) {
        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }

        final TemplateSearchEntity searchEntity = new TemplateSearchEntity();
        searchEntity.setText(text);
        searchEntity.setPage(currentPage);
        searchEntity.setPageSize(pageSize);
        searchEntity.setSortedField(sortedField);
        searchEntity.setIsDesc(isDesc);
        searchEntity.setFilterTimeField(filterTimeField);
        searchEntity.setBeginTime(beginTime);
        searchEntity.setEndTime(endTime);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(tags)) {
            searchEntity.setTags(Arrays.asList(tags.split(",")));
        }

        searchEntity.setDeleted(deleted); // 自定义应为false，或不提供，回收站中则设置为true
        searchEntity.setSourceType(sourceType);
        searchEntity.setTemplateType(templateType);

        final DataPage<TemplateDTO> page = searchEntity.getTempsByFilter(filterTemplates(PRE_FILTER));
        return Response.ok(page).build();
    }

    /**
     * 
     * 通用搜索
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response searchAll(@RequestBody TemplateSearchEntity searchEntity) {
        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }
        final DataPage<TemplateDTO> page = searchEntity.getTempsByFilter(filterTemplates(t -> true)); // 不做预过滤
        return Response.ok(page).build();

    }

    /**
     * 物理 强制删除 模板(会级联删除所有用户的关于此模板的收藏)
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{templateId}/force_delete")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response forceDeleteTemplate(@QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        final TemplateEntity requestTemplateEntity = new TemplateEntity();
        requestTemplateEntity.setId(templateId);

        return withWriteLock(//
                serviceFacade, //
                requestTemplateEntity, //
                lookup -> {
                    final NiFiUser user = NiFiUserUtils.getNiFiUser();
                    final Authorizable template = lookup.getTemplate(templateId);

                    // ensure write permission to the template
                    template.authorize(authorizer, RequestAction.WRITE, user);

                    // ensure write permission to the parent process group
                    template.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, user);
                }, //
                null, //
                (templateEntity) -> {
                    // delete the specified template
                    serviceFacade.deleteTemplate(templateEntity.getId());
                    // delete the references which be added to a favorite
                    deleteUserFavorityForTemplate(templateEntity.getId());
                    // build the response entity
                    final TemplateEntity entity = new TemplateEntity();

                    return generateOkResponse(entity).build();
                });
    }

    /**
     * 逻辑删除模板(仅仅添加标识)
     * 即将模板放入 模板回收站
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{templateId}/logic_delete")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response logicalDeleteTemplate(@QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        return setDeletedStateTempById(templateId, true);
    }

    /**
     * 模板逻辑恢复 (仅仅修改标识)
     * 即从模板回收站中找回
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{templateId}/recover")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response logicalRecoverTemplate(@QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        return setDeletedStateTempById(templateId, false);
    }

    // ================= these methods are used by create orchsym template ======================
    private SnippetAuthorizable authorizeSnippetUsage(final AuthorizableLookup lookup, final String groupId, final String snippetId, final boolean authorizeTransitiveServices) {
        final NiFiUser user = NiFiUserUtils.getNiFiUser();

        // ensure write access to the target process group
        lookup.getProcessGroup(groupId).getAuthorizable().authorize(authorizer, RequestAction.WRITE, user);

        // ensure read permission to every component in the snippet including referenced services
        final SnippetAuthorizable snippet = lookup.getSnippet(snippetId);
        authorizeSnippet(snippet, authorizer, lookup, RequestAction.READ, true, authorizeTransitiveServices);
        return snippet;
    }

    private Map<String, String> getContentsMapFromEntity(OrchsymCreateTemplateReqEntity entity) {
        Map<String, String> contentMap = new HashMap<>(7);
        if (entity.getCreatedUser() != null) {
            contentMap.put(AdditionConstants.KEY_CREATED_USER, entity.getCreatedUser());
        } else {
            contentMap.put(AdditionConstants.KEY_CREATED_USER, NiFiUserUtils.getNiFiUserIdentity());
        }

        if (entity.getCreatedTime() != null) {
            contentMap.put(AdditionConstants.KEY_CREATED_TIMESTAMP, Long.toString(entity.getCreatedTime()));
        } else {
            contentMap.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        }

        if (entity.getModifiedTime() != null) {
            contentMap.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, Long.toString(entity.getModifiedTime()));
        }
        if (entity.getModifiedUser() != null) {
            contentMap.put(AdditionConstants.KEY_MODIFIED_USER, entity.getModifiedUser());
        }

        if (entity.getUploadedUser() != null) {
            contentMap.put(TemplateFiledName.UPLOADED_USER, entity.getUploadedUser());
        }
        if (entity.getUploadedTime() != null) {
            contentMap.put(TemplateFiledName.UPLOADED_TIMESTAMP, Long.toString(entity.getUploadedTime()));
        }

        contentMap.put(TemplateFiledName.SOURCE_TYPE, TemplateSourceType.match(entity.getSourceType()).name());

        return contentMap;
    }

    private List<TemplateDTO> filterTemplates(Predicate<? super TemplateEntity> preFilter) {
        final List<TemplateDTO> notOfficalTemps = serviceFacade.getTemplates().stream()//
                .filter(preFilter)//
                .map(TemplateEntity::getTemplate)//
                .collect(Collectors.toList());

        final List<TemplateFavority> tempFavorites = getTempFavoriteByCurrentUser();
        notOfficalTemps.forEach(t -> {
            Map<String, String> additions = t.getAdditions();
            if (Objects.isNull(additions)) {
                additions = new HashMap<>();
            } else {
                additions = new HashMap<>(additions);
            }
            final boolean existed = tempFavorites.contains(new TemplateFavority(t.getId(), 0L));
            additions.put(TemplateFiledName.IS_FAVORITE, Boolean.valueOf(existed).toString());
            t.setAdditions(additions);
        });

        return notOfficalTemps;
    }

    protected Boolean verifyTempNameValid(String newTempName, String templateId) {
        return !serviceFacade.getTemplates().stream()//
                .map(TemplateEntity::getTemplate)//
                .filter(dto -> {
                    if (templateId != null && !dto.getId().equals(templateId) && dto.getName().equals(newTempName)) {
                        return true;
                    }
                    if (templateId == null && dto.getName().equals(newTempName)) {
                        return true;
                    }
                    return false;
                })//
                .findAny()//
                .isPresent();

    }

    private List<TemplateFavority> getTempFavoriteByCurrentUser() {
        final Map<String, TreeList<TemplateFavority>> allUserFavorites = getFavoriteByCurrentUser();
        if (Objects.isNull(allUserFavorites)) {
            return Collections.emptyList();
        }
        TreeList<TemplateFavority> userFavList = allUserFavorites.get(NiFiUserUtils.getNiFiUserIdentity());
        if (Objects.isNull(userFavList) || userFavList.isEmpty()) {
            return Collections.emptyList();
        }
        return userFavList;

    }

    private Map<String, TreeList<TemplateFavority>> getFavoriteByCurrentUser() {
        final String additionStr = flowController.getRootGroup().getAddition(TemplateFiledName.KEY_USER_TEMP_FAV);
        if (additionStr == null) {
            return null;
        }

        return JSON.parseObject(additionStr, new TypeReference<Map<String, TreeList<TemplateFavority>>>() {
        });
    }

    /**
     * 删除涉及指定模板的所有收藏
     * 
     * @param templateId
     */
    private void deleteUserFavorityForTemplate(String templateId) {
        final Map<String, TreeList<TemplateFavority>> allUserFavorites = getFavoriteByCurrentUser();
        if (Objects.isNull(allUserFavorites)) {
            return;
        }
        TreeList<TemplateFavority> userFavList = allUserFavorites.get(NiFiUserUtils.getNiFiUserIdentity());
        if (Objects.isNull(userFavList)) {
            return;
        }
        userFavList.remove(new TemplateFavority(templateId, 0L));

        flowController.getRootGroup().setAddition(TemplateFiledName.KEY_USER_TEMP_FAV, JSONObject.toJSONString(allUserFavorites));
        // save
        flowService.saveFlowChanges(TimeUnit.SECONDS, 0, true);
    }

    private Response setDeletedStateTempById(String templateId, boolean deleted) {
        final TemplateEntity requestTemplateEntity = new TemplateEntity();
        requestTemplateEntity.setId(templateId);

        return withWriteLock(//
                serviceFacade, //
                requestTemplateEntity, //
                lookup -> {
                    final NiFiUser user = NiFiUserUtils.getNiFiUser();
                    final Authorizable template = lookup.getTemplate(templateId);
                    template.authorize(authorizer, RequestAction.WRITE, user);
                    template.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, user);
                }, //
                null, //
                (templateEntity) -> {
                    final Template template = templateDAO.getTemplate(templateId);
                    Map<String, String> additions = template.getDetails().getAdditions();
                    if (additions == null) {
                        additions = new HashMap<>();
                    } else {
                        additions = new HashMap<>(additions);
                    }
                    additions.put(AdditionConstants.KEY_IS_DELETED, Boolean.valueOf(deleted).toString());
                    template.getDetails().setAdditions(additions);

                    flowService.saveFlowChanges(TimeUnit.SECONDS, 0, true);
                    final TemplateDTO templateDTO = serviceFacade.getTemplate(templateId);
                    return generateOkResponse(templateDTO).build();
                });
    }

}
