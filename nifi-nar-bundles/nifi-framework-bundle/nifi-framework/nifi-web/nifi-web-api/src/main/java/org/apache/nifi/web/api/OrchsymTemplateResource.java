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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.collections4.list.TreeList;
import org.apache.nifi.additions.StandardTypeAdditions;
import org.apache.nifi.authorization.AuthorizableLookup;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.SnippetAuthorizable;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.Template;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.security.xml.XmlUtils;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.util.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.OrchsymCreateTemplateReqEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.orchsym.DataPage;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.api.orchsym.template.OrchsymTemplateEntity;
import org.apache.nifi.web.api.orchsym.template.TemplateFavority;
import org.apache.nifi.web.api.orchsym.template.TemplateFieldName;
import org.apache.nifi.web.api.orchsym.template.TemplateSearchEntity;
import org.apache.nifi.web.api.orchsym.template.TemplateSourceType;
import org.apache.nifi.web.dao.TemplateDAO;
import org.apache.nifi.web.util.ChinesePinyinUtil;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import io.swagger.annotations.Api;

/**
 * @author liuxun
 * @apiNote 处理connections清除队列的相关功能
 */
@Component
@Path("/orchsym-template")
@Api(value = "/orchsym-template", description = "for Template")
public class OrchsymTemplateResource extends AbsOrchsymResource {
    private static final Logger logger = LoggerFactory.getLogger(OrchsymTemplateResource.class);

    @Autowired
    private TemplateResource templateResource;

    @Autowired
    private TemplateDAO templateDAO;

    @Autowired
    private FlowService flowService;

    /**
     * 模块保存为模板
     */
    @POST
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{parentGroupId}/saveas")
    public Response createTemplate(//
            @Context final HttpServletRequest httpServletRequest, //
            @PathParam("parentGroupId") final String parentGroupId, //
            @RequestBody final OrchsymCreateTemplateReqEntity requestCreateTemplateRequestEntity//
    ) {

        if (requestCreateTemplateRequestEntity.getSnippetId() == null) {
            throw new IllegalArgumentException("The snippet identifier must be specified.");
        }

        if (StringUtils.isBlank(requestCreateTemplateRequestEntity.getName())) {
            throw new IllegalArgumentException("The template name must be specified.");
        }

        // Avoid the timestamp is different for cluster
        if (requestCreateTemplateRequestEntity.getCreatedTime() == null) {
            requestCreateTemplateRequestEntity.setCreatedTime(System.currentTimeMillis());
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
                    authorizeSnippetUsage(lookup, parentGroupId, requestCreateTemplateRequestEntity.getSnippetId(), true);
                }, //
                () -> {
                    if (!requestCreateTemplateRequestEntity.isOverwrite()) { // 如果不是覆盖，则验证名字，否则忽略，并将稍后删除存在同名的
                        serviceFacade.verifyCanAddTemplate(parentGroupId, requestCreateTemplateRequestEntity.getName());
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

                    final ProcessGroup parentGroup = flowController.getGroup(parentGroupId);
                    // 构建默认设置
                    final Map<String, String> additions = TemplateFieldName.getCreatedAdditions(createTemplateRequestEntity, parentGroup.isRootGroup(), NiFiUserUtils.getNiFiUserIdentity());

                    final Set<String> tags = createTemplateRequestEntity.getTags();
                    // create the template and generate the json
                    final TemplateDTO template = serviceFacade.createTemplate(additions, tags, newName, createTemplateRequestEntity.getDescription(), createTemplateRequestEntity.getSnippetId(),
                            parentGroupId, getIdGenerationSeed());
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
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/edit")
    public Response editTemplate(//
            @RequestBody final TemplateDTO tempParam//
    ) {

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

        return withWriteLock( //
                serviceFacade, //
                requestTemplateEntity, //
                lookup -> {
                    final Authorizable temp = lookup.getTemplate(tempParam.getId());

                    // ensure write permission to the template
                    temp.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());

                    // ensure write permission to the parent process group
                    temp.getParentAuthorizable().authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                }, //
                null, //
                (templateEntity) -> {
                    final Map<String, String> additions = originTempDTO.getAdditions() == null ? new HashMap<>() : originTempDTO.getAdditions();
                    additions.put(AdditionConstants.KEY_MODIFIED_USER, tempParam.getAdditions().get(AdditionConstants.KEY_MODIFIED_USER));
                    additions.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, tempParam.getAdditions().get(AdditionConstants.KEY_MODIFIED_TIMESTAMP));

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
     * @param name
     *            编辑或创建模板时的新命名
     * @param templateId
     *            编辑时，当前模板id
     * @return
     *         在创建时，一般为空；
     *         在编辑时，为了验证重命名，需要排除自己，所以需要提供当前模板id
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/name/valid")
    public Response isTemplateNewNameValid(//
            @QueryParam("name") String name, //
            @QueryParam("templateId") String templateId//
    ) {
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
     * 参数默认值，需同TemplateSearchEntity中设置
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/custom/search")
    public Response searchCustomTempsByQueryParams(//
            @QueryParam("text") String text, //

            // page
            @QueryParam("page") @DefaultValue("1") int currentPage, //
            @QueryParam("pageSize") @DefaultValue("10") int pageSize, //

            // sort, name createdTime modifiedTime uploadedTime
            @QueryParam("sortedField") @DefaultValue("createdTime") String sortedField, // 创建时间
            @QueryParam("isDesc") @DefaultValue("true") boolean isDesc, // 降序
            @QueryParam("deleted") @DefaultValue("false") boolean deleted, // 未删除

            @QueryParam("templateType") String templateType, //
            @QueryParam("sourceType") String sourceType, //

            // filter， createdTime modifiedTime uploadedTime
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
        searchEntity.setDesc(isDesc);
        searchEntity.setFilterTimeField(filterTimeField);
        searchEntity.setBeginTime(beginTime);
        searchEntity.setEndTime(endTime);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(tags)) {
            searchEntity.setTags(Arrays.asList(tags.split(",")));
        }

        searchEntity.setDeleted(deleted); // 自定义应为false，或不提供，回收站中则设置为true
        searchEntity.setSourceType(sourceType);
        searchEntity.setTemplateType(templateType);

        final DataPage<TemplateDTO> page = searchEntity.getTempsByFilter(filterTemplates(PRE_FILTER), ChinesePinyinUtil.zhComparator);
        return Response.ok(page).build();
    }

    /**
     * 
     * 通用搜索
     */
    @POST
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/search")
    public Response searchAll(//
            @RequestBody final TemplateSearchEntity searchEntity//
    ) {
        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node has been disconnected from cluster").build();
        }
        if (!StringUtils.isEmpty(searchEntity.getText())) {
            try {// 解决其他api调用时候中文乱码
                searchEntity.setText(URLDecoder.decode(searchEntity.getText(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }
        final DataPage<TemplateDTO> page = searchEntity.getTempsByFilter(filterTemplates(t -> true), ChinesePinyinUtil.zhComparator); // 不做预过滤
        return Response.ok(page).build();

    }

    /**
     * 物理 强制删除 模板(会级联删除所有用户的关于此模板的收藏)
     */
    @DELETE
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{templateId}/force_delete")
    public Response forceDeleteTemplate(//
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged, //
            @PathParam("templateId") final String templateId//
    ) {
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
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{templateId}/logic_delete")
    public Response logicalDeleteTemplate(//
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged, //
            @PathParam("templateId") final String templateId//
    ) {
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
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/{templateId}/recover")
    public Response logicalRecoverTemplate(//
            @QueryParam(DISCONNECTED_NODE_ACKNOWLEDGED) @DefaultValue("false") final Boolean disconnectedNodeAcknowledged, //
            @PathParam("templateId") final String templateId//
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.PUT);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(disconnectedNodeAcknowledged);
        }

        return setDeletedStateTempById(templateId, false);
    }

    /**
     * 仅将上传的模板xml文件 解析为json 对象(TemplateDTO) 返给前端
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/xml_parse")
    public Response parseTemplateXMLFile(//
            @FormDataParam("template") final InputStream in//
    ) throws InterruptedException {
        // only parse no need to replicate cluster nodes
        if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node is disconnected from cluster").build();
        }

        // unmarshal the template
        final TemplateDTO template;
        try {
            JAXBContext context = JAXBContext.newInstance(TemplateDTO.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLStreamReader xsr = XmlUtils.createSafeReader(in);
            JAXBElement<TemplateDTO> templateElement = unmarshaller.unmarshal(xsr, TemplateDTO.class);
            template = templateElement.getValue();
        } catch (JAXBException jaxbe) {
            logger.warn("An error occurred while parsing a template.", jaxbe);
            return Response.status(Response.Status.BAD_REQUEST).entity("The specified template is not in a valid format.").build();
        } catch (IllegalArgumentException iae) {
            logger.warn("Unable to import template.", iae);
            return Response.status(Response.Status.BAD_REQUEST).entity("invalid argument because of " + iae.getMessage()).build();
        } catch (Exception e) {
            logger.warn("An error occurred while importing a template.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(template).build();
    }

    /**
     * 上传json 导入模板
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/json_import")
    public Response importTemplateFromJson(//
            @PathParam("groupId") String groupId, //
            final OrchsymTemplateEntity requestTemplateEntity//
    ) {

        // verify the template was specified
        if (requestTemplateEntity == null || requestTemplateEntity.getTemplate() == null || requestTemplateEntity.getTemplate().getSnippet() == null) {
            throw new IllegalArgumentException("Template details must be specified.");
        }

        if (Objects.isNull(requestTemplateEntity.getUploadedTime())) {
            requestTemplateEntity.setUploadedTime(System.currentTimeMillis());
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, requestTemplateEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestTemplateEntity.isDisconnectedNodeAcknowledged());
        }

        final String realGroupId = FlowController.ROOT_GROUP_ID_ALIAS.equals(groupId) ? flowController.getRootGroupId() : groupId;

        return withWriteLock(//
                serviceFacade, //
                requestTemplateEntity, //
                lookup -> {
                    final Authorizable processGroup = lookup.getProcessGroup(groupId).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                }, //
                () -> serviceFacade.verifyCanAddTemplate(realGroupId, requestTemplateEntity.getTemplate().getName()), //
                templateEntity -> {
                    try {
                        Map<String, String> additions = templateEntity.getTemplate().getAdditions();
                        if (null != additions) {
                            additions = new HashMap<>();
                        } else {
                            additions = new HashMap<>(additions);
                        }
                        final Map<String, String> uploadedAdditions = TemplateFieldName.getUploadedAdditions(requestTemplateEntity, NiFiUserUtils.getNiFiUserIdentity());
                        additions.putAll(uploadedAdditions);
                        templateEntity.getTemplate().setAdditions(additions);

                        // import the template
                        final TemplateDTO template = serviceFacade.importTemplate(templateEntity.getTemplate(), groupId, getIdGenerationSeed());
                        templateResource.populateRemainingTemplateContent(template);

                        // build the response entity
                        TemplateEntity entity = new TemplateEntity();
                        entity.setTemplate(template);

                        // build the response
                        return generateCreatedResponse(URI.create(template.getUri()), entity).build();
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        logger.warn("Unable to import template.", e);
                        return Response.status(Response.Status.BAD_REQUEST).entity("invalid argument because of " + e.getMessage()).build();
                    } catch (Exception e) {
                        logger.warn("An error occurred while importing a template.", e);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
                    }
                });
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

    private List<TemplateDTO> filterTemplates(Predicate<? super TemplateEntity> preFilter) {
        final List<TemplateDTO> notOfficalTemps = serviceFacade.getTemplates().stream()//
                .filter(preFilter)//
                .map(TemplateEntity::getTemplate)//
                .collect(Collectors.toList());

        final List<TemplateFavority> tempFavorites = getTempFavoriteByCurrentUser();
        notOfficalTemps.forEach(t -> {
            StandardTypeAdditions additions = new StandardTypeAdditions(t.getAdditions());

            // 临时设置为前端提供收藏标记
            final boolean existed = tempFavorites.contains(new TemplateFavority(t.getId(), 0L));
            additions.setValue(TemplateFieldName.IS_FAVORITE, existed);

            // 尽量为前端提供创建时间
            if (!additions.has(AdditionConstants.KEY_CREATED_TIMESTAMP)) {
                Long time = null;
                // 先尝试原始创建时间戳
                if (additions.has(AdditionConstants.KEY_ORIGINAL_CREATED_TIMESTAMP)) {
                    time = ProcessUtil.getAdditionLongValue(additions, AdditionConstants.KEY_ORIGINAL_CREATED_TIMESTAMP, null);
                }
                // 尝试上传时间
                if (Objects.isNull(time) && additions.has(TemplateFieldName.UPLOADED_TIMESTAMP)) {
                    time = ProcessUtil.getAdditionLongValue(additions, TemplateFieldName.UPLOADED_TIMESTAMP, null);
                }
                if (!Objects.isNull(time)) {
                    additions.setValue(AdditionConstants.KEY_CREATED_TIMESTAMP, time);
                }
            }

            t.setAdditions(additions.values());
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
        final String additionStr = flowController.getRootGroup().getAdditions().getValue(TemplateFieldName.KEY_USER_TEMP_FAV);
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

        flowController.getRootGroup().getAdditions().setValue(TemplateFieldName.KEY_USER_TEMP_FAV, JSONObject.toJSONString(allUserFavorites));
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
