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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import org.apache.nifi.web.api.orchsym.template.*;
import org.apache.nifi.web.dao.TemplateDAO;
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
            requestCreateTemplateRequestEntity.setSourceType(TemplateSourceType.SAVE_AS_TYPE.value());
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
                }, () -> {
                    if (!requestCreateTemplateRequestEntity.isOverwrite()) {
                        serviceFacade.verifyCanAddTemplate(groupId, requestCreateTemplateRequestEntity.getName());
                    }
                }, createTemplateRequestEntity -> {
                    // 如果设置了overwrite为true，先进行重名模板的清理
                    if (createTemplateRequestEntity.isOverwrite()) {
                        deleteTemplatesByName(createTemplateRequestEntity.getName());
                    }

                    final Map<String, String> contentsMap = getContentsMapFromEntity(createTemplateRequestEntity);
                    final ProcessGroup group = flowController.getGroup(groupId);
                    if (group.getParent().isRootGroup()) {
                        contentsMap.put(TemplateFiledName.TEMPLATE_TYPE, Long.toString(TemplateType.APP_TYPE.value()));
                    } else {
                        contentsMap.put(TemplateFiledName.TEMPLATE_TYPE, Long.toString(TemplateType.NON_APP_TYPE.value()));
                    }

                    final Set<String> tagsSet = createTemplateRequestEntity.getTags();
                    // create the template and generate the json
                    final TemplateDTO template = serviceFacade.createTemplate(contentsMap, tagsSet, createTemplateRequestEntity.getName(), createTemplateRequestEntity.getDescription(),
                            createTemplateRequestEntity.getSnippetId(), groupId, getIdGenerationSeed());
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

            @QueryParam("deleted") @DefaultValue("false") Boolean deleted, //
            @QueryParam("templateType") Integer templateType, //

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
        searchEntity.setTemplateType(templateType);
        searchEntity.setDeleted(deleted);
        if (!org.apache.commons.lang3.StringUtils.isBlank(tags)) {
            searchEntity.setTags(Arrays.asList(tags.split(",")));
        }
        return getCustomTypeTemplates(searchEntity);
    }

    /**
     * 对自定义类型的模板进行 POST方式查询
     * 
     * @param entity
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/custom/search")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response searchOfficeTempsByJsonEntity(TemplateSearchEntity entity) {
        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }
        return getCustomTypeTemplates(entity);
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
    public Response forceDeleteTemplate(
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId
            ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        final TemplateEntity requestTemplateEntity = new TemplateEntity();
        requestTemplateEntity.setId(templateId);

        return withWriteLock(
                serviceFacade,
                requestTemplateEntity,
                lookup -> {
                    final Authorizable template = lookup.getTemplate(templateId);

                    // ensure write permission to the template
                    template.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // ensure write permission to the parent process group
                    template.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                },
                null,
                (templateEntity) -> {
                    // delete the specified template
                    serviceFacade.deleteTemplate(templateEntity.getId());
                    // delete the references which be added to a favorite
                    deleteUserFavorityRefTemp(templateEntity.getId());
                    // build the response entity
                    final TemplateEntity entity = new TemplateEntity();

                    return generateOkResponse(entity).build();
                }
        );
    }

    /**
     *  逻辑删除模板(仅仅添加标识)
     *  即将模板放入 模板回收站
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{templateId}/logic_delete")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response logicalDeleteTemplate(
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId
            ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        return logicalDelOrRecoverTempById(templateId, LogicOperateTempType.OPERATE_LOGICAL_DELETE);
    }

    /**
     *  模板逻辑恢复 (仅仅修改标识)
     *  即从模板回收站中找回
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{templateId}/recover")
    @ApiResponses(value = { //
            @ApiResponse(code = 500, message = "server error") //
    })
    public Response logicalRecoverTemplate(
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged,
            @PathParam("templateId") final String templateId
            ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        return logicalDelOrRecoverTempById(templateId, LogicOperateTempType.OPERATE_LOGICAL_RECOVER);
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

        if (entity.getSourceType() != null) {
            contentMap.put(TemplateFiledName.SOURCE_TYPE, Integer.toString(entity.getSourceType()));
        }

        if (entity.getUploadedUser() != null) {
            contentMap.put(TemplateFiledName.UPLOADED_USER, entity.getUploadedUser());
        }

        if (entity.getUploadedTime() != null) {
            contentMap.put(TemplateFiledName.UPLOADED_TIMESTAMP, Long.toString(entity.getUploadedTime()));
        }

        return contentMap;
    }

    private Response getCustomTypeTemplates(TemplateSearchEntity searchEntity) {
        if (searchEntity == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("param cant be null").build();
        }

        final DataPage<TemplateDTO> page = searchEntity.getTempsByFilter(getNotOfficialTemplates());
        return Response.ok(page).build();
    }

    private List<TemplateDTO> getNotOfficialTemplates() {
        return serviceFacade.getTemplates().stream().filter(te -> {
            final Map<String, String> additions = te.getTemplate().getAdditions();
            if (additions != null && additions.containsKey(TemplateFiledName.SOURCE_TYPE)) {
                return Integer.parseInt(additions.get(TemplateFiledName.SOURCE_TYPE)) != TemplateSourceType.OFFICIAL_TYPE.value();
            }
            return true;
        }).map(TemplateEntity::getTemplate).collect(Collectors.toList());
    }

    protected Boolean verifyTempNameValid(String newTempName, String templateId) {
        Set<TemplateDTO> allDTOS = serviceFacade.getTemplates().stream().map(TemplateEntity::getTemplate).collect(Collectors.toSet());
        for (TemplateDTO dto : allDTOS) {
            if (templateId != null && !dto.getId().equals(templateId) && dto.getName().equals(newTempName)) {
                return false;
            }
            if (templateId == null && dto.getName().equals(newTempName)) {
                return false;
            }
        }
        return true;
    }

    protected void deleteTemplatesByName(String templateName) {
        serviceFacade.getTemplates().stream().map(TemplateEntity::getTemplate).collect(Collectors.toSet()).forEach(dto -> {
            if (dto.getName().equals(templateName)) {
                serviceFacade.deleteTemplate(dto.getId());
            }
        });
    }

    /**
     * 删除涉及指定模板的所有收藏
     * @param templateId
     */
    private void deleteUserFavorityRefTemp(String templateId) {
        final String KEY_USER_TEMP_FAV = "USER_TEMPLATES_FAVORITES";
        final String additionStr = flowController.getRootGroup().getAddition(KEY_USER_TEMP_FAV);
        if (additionStr == null){
            return;
        }

        final Map<String, TreeList<TemplateFavority>> allUserFavorites = JSON.parseObject(additionStr, new TypeReference<Map<String, TreeList<TemplateFavority>>>() {
        });

        final TemplateFavority favorite = new TemplateFavority();
        favorite.setTemplateId(templateId);
        favorite.setCreatedTime(0L);
        allUserFavorites.forEach((userId, favourites) ->{
            if (favourites.contains(favorite)){
                favourites.remove(favorite);
            }
        });

        flowController.getRootGroup().setAddition(KEY_USER_TEMP_FAV, JSONObject.toJSONString(allUserFavorites));
        // save
        flowService.saveFlowChanges(TimeUnit.SECONDS, 0, true);
    }

    private Response logicalDelOrRecoverTempById(String templateId, LogicOperateTempType type) {
        final TemplateEntity requestTemplateEntity = new TemplateEntity();
        requestTemplateEntity.setId(templateId);

        return withWriteLock(
                serviceFacade,
                requestTemplateEntity,
                lookup -> {
                    final Authorizable template = lookup.getTemplate(templateId);
                    template.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                    template.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                },
                null,
                (templateEntity) -> {
                    final Template template = templateDAO.getTemplate(templateId);
                    Map<String, String> additions = template.getDetails().getAdditions();
                    if (additions == null){
                        additions = new HashMap<>();
                    }
                    String isDeleteStr = type.equals(LogicOperateTempType.OPERATE_LOGICAL_DELETE) ? "true" : "false";
                    additions.put(AdditionConstants.KEY_IS_DELETED, isDeleteStr);
                    template.getDetails().setAdditions(new HashMap<>(additions));
                    flowService.saveFlowChanges(TimeUnit.SECONDS, 0, true);
                    final TemplateDTO templateDTO = serviceFacade.getTemplate(templateId);
                    return generateOkResponse(templateDTO).build();
                }
        );
    }

    private enum LogicOperateTempType{
        /**
         * 分别表示逻辑删除 和 逻辑恢复操作
         */
        OPERATE_LOGICAL_DELETE,
        OPERATE_LOGICAL_RECOVER

    }
}
