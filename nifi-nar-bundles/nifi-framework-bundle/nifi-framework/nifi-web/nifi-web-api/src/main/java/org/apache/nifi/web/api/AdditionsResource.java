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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.nifi.authorization.resource.ResourceType;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

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
public class AdditionsResource extends AbsOrchsymResource {
    public static final String KEY_ID = "id";
    public static final String KEY_KEY = "key";
    public static final String KEY_VALUE = "value";

    @Autowired
    private FlowService flowService;

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/{key}")
    @ApiOperation(value = "get value of addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response getAdditionsByGroupId(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("key") String key //
    ) {

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

        key = key.toUpperCase();

        String content = null;
        final Map<String, String> additions = group.getAdditions();
        if (null != additions) {
            content = additions.get(key);
        }

        JSONObject result = new JSONObject();
        result.put(KEY_ID, groupId);
        result.put(KEY_KEY, key);
        result.put(KEY_VALUE, StringUtils.isBlank(content) ? "" : content);
        return noCache(Response.ok(result.toJSONString())).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    @Path("/{groupId}/{key}/status")
    @ApiOperation(value = "get value of addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 204, message = "No contents"), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })

    public Response getAdditionsStatusByGroupId(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("key") String key //
    ) {

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

        key = key.toUpperCase();

        final Map<String, String> additions = group.getAdditions();
        if (null == additions || !additions.containsKey(key) || StringUtils.isBlank(additions.get(key))) {
            return Response.noContent().build(); // 204
        }

        return generateOkResponse().build();
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/{key}")
    @ApiOperation(value = "add addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response setAdditions(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") final String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("key") final String key, //
            @ApiParam(value = "The value of addition for the key", required = true) @RequestBody final String content //
    ) {
        return writeAdditions(groupId, key, content, false);
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/{key}")
    @ApiOperation(value = "add addition in process group", //
            response = JSONObject.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 404, message = CODE_MESSAGE_404) //
    })
    public Response deleteAdditions(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "the group id which group to add additions") @PathParam("groupId") final String groupId, //
            @ApiParam(value = "The key of addition") @PathParam("key") final String key //
    ) {
        return writeAdditions(groupId, key, null, true);
    }

    private Response writeAdditions(String groupId, String key, String content, boolean deleted) {
        final ProcessGroup group = (FlowController.ROOT_GROUP_ID_ALIAS.equals(groupId)) ? flowController.getRootGroup() : flowController.getGroup(groupId);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("not found the group by groupId").build(); // 404
        }
        groupId = group.getIdentifier(); // get the real id

        try {
            authorize(ResourceType.ProcessGroup, RequestAction.WRITE, groupId);
            // serviceFacade.authorizeAccess(lookup -> {
            // final Authorizable flow = lookup.getProcessGroup(groupId).getAuthorizable();
            // flow.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
            // });
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // 401
        }

        if (isReplicateRequest()) {
            replicate(HttpMethod.POST, content);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node is disconnected from cluster").build(); // 400
        }

        key = key.toUpperCase();

        Map<String, String> additions = new HashMap<>();
        if (null != group.getAdditions()) {
            additions = new HashMap<>(group.getAdditions());
        }

        content = StringUtils.isBlank(content) ? "" : content;

        final String oldValue = additions.get(key);
        if (deleted) {
            additions.remove(key);
        } else { // modify
            additions.put(key, content); // replace
        }
        group.setAdditions(additions);

        flowService.saveFlowChanges(TimeUnit.SECONDS, 0L, true);

        JSONObject result = new JSONObject();
        result.put(KEY_ID, groupId);
        result.put(KEY_KEY, key);
        if (deleted) {
            result.put(KEY_VALUE, StringUtils.isBlank(oldValue) ? "" : oldValue);
        } else {
            result.put("oldValue", StringUtils.isBlank(oldValue) ? "" : oldValue);
            result.put("newValue", content);
        }
        return generateOkResponse(result.toJSONString()).build();
    }

}
