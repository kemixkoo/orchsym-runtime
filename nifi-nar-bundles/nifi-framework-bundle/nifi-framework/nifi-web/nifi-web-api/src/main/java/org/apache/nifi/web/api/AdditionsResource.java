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
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.services.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liuxun
 * @apiNote 为指定的processGroup添加额外的持久化配置
 */
@Component
@Path("/additions")
@Api(value = "/additions", description = "operate additions in group")
public class AdditionsResource extends AbsOrchsymResource {

    @Autowired
    private FlowService flowService;

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}")
    @ApiOperation(value = "add addition in process group", //
            response = Response.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response putAdditionsByGroupId(
        @Context final HttpServletRequest httpServletRequest,
         @ApiParam(value = "the group id which group to add additions") @PathParam("groupId")String groupId,
         @ApiParam(value = "The map  contents about additions", required = true) final Map<String, String> additionsMap,
        @ApiParam(value = "the group id which group to add additions") @QueryParam("overwrite") @DefaultValue("false") Boolean overwrite
        ) {

        if (isReplicateRequest()) {
            replicate(HttpMethod.POST, additionsMap);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node is disconnected from cluster").build();
        }

        final ProcessGroup group = ("root".equals(groupId))? flowController.getRootGroup() : flowController.getGroup(groupId);
        if (group == null){
            return  Response.status(Response.Status.NOT_FOUND).entity("not found the group by groupId").build();
        }
        final Map<String, String> originAdditions = group.getAdditions();
        Map<String,String> putMap = new HashMap<>();
        /**
         * 如果overwrite为true，则完全覆盖
         * 否则是在之前的基础之上添加
         */
        if (!overwrite && originAdditions != null){
            // first put origin
            putMap.putAll(originAdditions);
        }
        // keep param map is new
        putMap.putAll(additionsMap);
        group.setAdditions(putMap);
        flowService.saveFlowChanges(TimeUnit.SECONDS,0L, true);
        return generateOkResponse(putMap).build();

    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}")
    @ApiOperation(value = "get addition in process group", //
            response = Response.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getAdditionsByGroupId(
        @Context final HttpServletRequest httpServletRequest,
         @ApiParam(value = "the group id which group to add additions") @PathParam("groupId")String groupId) {

        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        } else if (isDisconnectedFromCluster()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("current node is disconnected from cluster").build();
        }

        final ProcessGroup group = ("root".equals(groupId))? flowController.getRootGroup() : flowController.getGroup(groupId);
        if (group == null){
            return  Response.status(Response.Status.NOT_FOUND).entity("not found the group by groupId").build();
        }
        Map<String, String> additions = group.getAdditions();
        additions = (additions == null) ? new HashMap<>() : additions;

        return noCache(Response.ok(additions)).build();
    }


}
