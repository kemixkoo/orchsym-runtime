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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.resource.ResourceType;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.groups.ProcessAdditions;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.api.entity.AdditionConfEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author liuxun
 * @apiNote 为指定的processGroup添加额外的持久化配置
 */
@Component
@Path("/additions")
@Api(value = "/additions", description = "operate additions in group")
public class OrchsymAdditionsResource extends AbsOrchsymResource {
    public static final String KEY_ID = "id";

    @Autowired
    private FlowService flowService;

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/{name}")
    @ApiOperation(value = "get value of addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAdditionsByGroupId(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("name") String name //
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        final ProcessGroup group = (FlowController.ROOT_GROUP_ID_ALIAS.equals(groupId)) ? flowController.getRootGroup() : flowController.getGroup(groupId);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("not found the group by groupId").build(); // 404
        }
        groupId = group.getIdentifier(); // get the real id

        try {
            authorize(ResourceType.ProcessGroup, RequestAction.READ, groupId);
            // serviceFacade.authorizeAccess(lookup -> {
            // final Authorizable flow = lookup.getProcessGroup(groupId).getAuthorizable();
            // flow.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
            // });
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // 401
        }

        name = name.toUpperCase();

        String content = null;
        final Map<String, String> additions = group.getAdditions();
        if (null != additions) {
            content = additions.get(name);
        }

        JSONObject result = new JSONObject();
        result.put(KEY_ID, groupId);
        result.put(ProcessAdditions.ADDITION_KEY_NAME, name);
        result.put(ProcessAdditions.ADDITION_VALUE_NAME, StringUtils.isBlank(content) ? "" : content);
        return noCache(Response.ok(result.toJSONString())).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    @Path("/{groupId}/{name}/status")
    @ApiOperation(value = "get value of addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 204, message = "No contents"), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })

    public Response getAdditionsStatusByGroupId(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("name") String name //
    ) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }
        final ProcessGroup group = (FlowController.ROOT_GROUP_ID_ALIAS.equals(groupId)) ? flowController.getRootGroup() : flowController.getGroup(groupId);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("not found the group by groupId").build(); // 404
        }
        groupId = group.getIdentifier(); // get the real id

        try {
            authorize(ResourceType.ProcessGroup, RequestAction.READ, groupId);
            // serviceFacade.authorizeAccess(lookup -> {
            // final Authorizable flow = lookup.getProcessGroup(groupId).getAuthorizable();
            // flow.authorize(authorizer, RequestAction.READ, NiFiUserUtils.getNiFiUser());
            // });
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // 401
        }

        if (ProcessUtil.hasValueGroupAdditions(group, name)) {
            return Response.noContent().build(); // 204
        }

        return generateOkResponse().build();
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/group/{groupId}")
    @ApiOperation(value = "add addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response changeGroupAdditions(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") String groupId, //
            AdditionConfEntity confEntity) {
        if (StringUtils.isBlank(confEntity.getId())) {
            confEntity.setId(groupId);
        } else if (!groupId.equals(confEntity.getId())) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        return modifyGroupAdditions(httpServletRequest, confEntity);
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/group")
    @ApiOperation(value = "add addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response modifyGroupAdditions(@Context final HttpServletRequest httpServletRequest, //
            AdditionConfEntity confEntity) {
        final ProcessGroup group = (FlowController.ROOT_GROUP_ID_ALIAS.equals(confEntity.getId())) ? flowController.getRootGroup() : flowController.getGroup(confEntity.getId());
        if (null == group) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, confEntity);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node is disconnected from cluster").build(); // 400
        }

        return withWriteLock(serviceFacade, confEntity, //
                lookup -> {
                    final NiFiUser user = NiFiUserUtils.getNiFiUser();
                    final Authorizable processGroup = lookup.getProcessGroup(group.getIdentifier()).getAuthorizable();
                    processGroup.authorize(authorizer, RequestAction.WRITE, user);
                }, null, //
                entity -> {
                    final String id = entity.getId();
                    final String name = entity.getName();
                    final String value = entity.getValue();
                    final boolean isDelete = entity.isDelete();

                    String oldValue = null;
                    if (isDelete) {
                        oldValue = ProcessUtil.removeGroupAdditions(group, name);
                    } else { // modify
                        oldValue = ProcessUtil.updateGroupAdditions(group, name, value);
                    }

                    flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);

                    JSONObject result = new JSONObject();
                    result.put(KEY_ID, id);
                    result.put(ProcessAdditions.ADDITION_KEY_NAME, name);
                    if (isDelete) {
                        result.put(ProcessAdditions.ADDITION_VALUE_NAME, StringUtils.isBlank(oldValue) ? "" : oldValue);
                    } else {
                        result.put("oldValue", StringUtils.isBlank(oldValue) ? "" : oldValue);
                        result.put("newValue", value);
                    }
                    return generateOkResponse(result.toJSONString()).build();
                });
    }

}
