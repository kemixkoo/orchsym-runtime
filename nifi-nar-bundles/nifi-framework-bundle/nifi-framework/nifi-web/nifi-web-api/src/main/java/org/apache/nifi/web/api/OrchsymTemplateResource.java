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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.authorization.AuthorizableLookup;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.SnippetAuthorizable;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.OrchsymCreateTemplateReqEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.api.orchsym.template.TemplateFiledName;
import org.apache.nifi.web.api.orchsym.template.TemplateSourceType;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}")
    @ApiOperation(
            value = "Creates a template and discards the specified snippet.",
            response = TemplateEntity.class,
            authorizations = {
                    @Authorization(value = "Write - /process-groups/{uuid}"),
                    @Authorization(value = "Read - /{component-type}/{uuid} - For each component in the snippet and their descendant components")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
                    @ApiResponse(code = 401, message = "Client could not be authenticated."),
                    @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
                    @ApiResponse(code = 404, message = "The specified resource could not be found."),
                    @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.")
            }
    )
    public Response createTemplate(
            @Context final HttpServletRequest httpServletRequest,
            @ApiParam(
                    value = "The process group id.",
                    required = true
            )
            @PathParam("groupId") final String groupId,
            @ApiParam(
                    value = "The create template request.",
                    required = true
            ) final OrchsymCreateTemplateReqEntity requestCreateTemplateRequestEntity) {

        if (requestCreateTemplateRequestEntity.getSnippetId() == null) {
            throw new IllegalArgumentException("The snippet identifier must be specified.");
        }

        if (requestCreateTemplateRequestEntity.getCreatedTime() == null){
            requestCreateTemplateRequestEntity.setCreatedTime(System.currentTimeMillis());
        }

        if (requestCreateTemplateRequestEntity.getCreatedUser() == null){
            requestCreateTemplateRequestEntity.setCreatedUser(NiFiUserUtils.getNiFiUserIdentity());
        }

        if (requestCreateTemplateRequestEntity.getSourceType() == null){
            // 创建模板默认属于 另存类型
            requestCreateTemplateRequestEntity.setSourceType(TemplateSourceType.SAVE_AS_TYPE.value());
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, requestCreateTemplateRequestEntity);
        } else if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(requestCreateTemplateRequestEntity.isDisconnectedNodeAcknowledged());
        }

        return withWriteLock(
                serviceFacade,
                requestCreateTemplateRequestEntity,
                lookup -> {
                    authorizeSnippetUsage(lookup, groupId, requestCreateTemplateRequestEntity.getSnippetId(), true);
                },
                () -> serviceFacade.verifyCanAddTemplate(groupId, requestCreateTemplateRequestEntity.getName()),
                createTemplateRequestEntity -> {
                    final Map<String, String> contentsMap = getContentsMapFromEntity(createTemplateRequestEntity);
                    final Set<String> tagsSet = createTemplateRequestEntity.getTags();
                    // create the template and generate the json
                    final TemplateDTO template = serviceFacade.createTemplate(contentsMap,tagsSet, createTemplateRequestEntity.getName(), createTemplateRequestEntity.getDescription(),
                            createTemplateRequestEntity.getSnippetId(), groupId, getIdGenerationSeed());
                    templateResource.populateRemainingTemplateContent(template);

                    // build the response entity
                    final TemplateEntity entity = new TemplateEntity();
                    entity.setTemplate(template);

                    // build the response
                    return generateCreatedResponse(URI.create(template.getUri()), entity).build();
                }
        );
    }

    private SnippetAuthorizable authorizeSnippetUsage(final AuthorizableLookup lookup, final String groupId, final String snippetId, final boolean authorizeTransitiveServices) {
        final NiFiUser user = NiFiUserUtils.getNiFiUser();

        // ensure write access to the target process group
        lookup.getProcessGroup(groupId).getAuthorizable().authorize(authorizer, RequestAction.WRITE, user);

        // ensure read permission to every component in the snippet including referenced services
        final SnippetAuthorizable snippet = lookup.getSnippet(snippetId);
        authorizeSnippet(snippet, authorizer, lookup, RequestAction.READ, true, authorizeTransitiveServices);
        return snippet;
    }

    private Map<String, String> getContentsMapFromEntity(OrchsymCreateTemplateReqEntity entity){
        Map<String, String> contentMap = new HashMap<>(7);
        if (entity.getCreatedUser() != null){
            contentMap.put(AdditionConstants.KEY_CREATED_USER, entity.getCreatedUser());
        }

        if (entity.getCreatedTime() != null){
            contentMap.put(AdditionConstants.KEY_CREATED_TIMESTAMP, Long.toString(entity.getCreatedTime()));
        }

        if (entity.getModifiedTime() != null){
            contentMap.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, Long.toString(entity.getModifiedTime()));
        }

        if (entity.getModifiedUser() != null){
            contentMap.put(AdditionConstants.KEY_MODIFIED_USER, entity.getModifiedUser());
        }

        if (entity.getSourceType() != null){
            contentMap.put(TemplateFiledName.SOURCE_TYPE, Integer.toString(entity.getSourceType()));
        }

        if (entity.getUploadedUser() != null){
            contentMap.put(TemplateFiledName.UPLOADED_USER, entity.getUploadedUser());
        }

        if (entity.getUploadedTime() != null){
            contentMap.put(TemplateFiledName.UPLOADED_TIMESTAMP, Long.toString(entity.getUploadedTime()));
        }

        return contentMap;
    }
}
