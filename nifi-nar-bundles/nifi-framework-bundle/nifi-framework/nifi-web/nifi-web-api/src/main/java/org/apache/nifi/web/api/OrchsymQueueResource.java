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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.connectable.Connectable;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionStatusEntity;
import org.apache.nifi.web.api.entity.QueueSnippetEntity;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;

/**
 * @author liuxun
 * @apiNote 处理connections清除队列的相关功能
 */
@Component
@Path("/queue")
@Api(value = "/queue", description = "clear queues of relational connections")
public class OrchsymQueueResource extends AbsOrchsymResource {
    public static final String KEY_CONNS = "connections";
    public static final String KEY_CONN = "connection";

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response getConnectionsStatus(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The selected queue and groups.", required = true) final QueueSnippetEntity queueSnippetEntity) {
        if (isDisconnectedFromCluster()) {
            verifyDisconnectedNodeModification(queueSnippetEntity.getDisconnectedNodeAcknowledged());
        }

        Set<String> canClearQueueConnIds = new HashSet<>();
        if (queueSnippetEntity.getConnectionIds() != null) {
            for (String connId : queueSnippetEntity.getConnectionIds()) {
                if (isCanClearQueue(connId)) {
                    canClearQueueConnIds.add(connId);
                }
            }
        }

        if (queueSnippetEntity.getProcessGroupIds() != null) {
            for (String groupId : queueSnippetEntity.getProcessGroupIds()) {
                getConnectionIdsFromGroup(groupId, canClearQueueConnIds);
            }
        }
        JSONObject result = new JSONObject();
        result.put(KEY_CONNS, canClearQueueConnIds);
        return noCache(Response.ok(result.toJSONString())).build();

    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response cleanQueue(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The connection id", required = true) @PathParam("id") final String connectionId) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.DELETE);
        }
        ConnectionEntity connEntity = new ConnectionEntity();
        connEntity.setId(connectionId);
        return withWriteLock(//
                serviceFacade, //
                connEntity, //
                lookup -> {
                    final Authorizable rootProcessGroup = lookup.getConnection(connectionId).getAuthorizable();
                    rootProcessGroup.authorize(authorizer, RequestAction.WRITE, NiFiUserUtils.getNiFiUser());
                }, //
                null, //
                (enity) -> {
                    DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(enity.getId(), UUID.randomUUID().toString());
                    serviceFacade.deleteFlowFileDropRequest(enity.getId(), dropRequest.getId());

                    JSONObject result = new JSONObject();
                    result.put(KEY_CONN, connectionId);
                    result.put("status", Response.Status.OK);
                    return generateOkResponse(result.toJSONString()).build();
                });

    }

    // private methods

    /**
     * @param connectionId
     * @return
     * @apiNote 判断是否可以清除指定连接的队列数据
     */
    private boolean isCanClearQueue(String connectionId) {
        return isCanClearQueue(flowController.getConnection(connectionId));
    }

    private boolean isCanClearQueue(final Connection connection) {
        if (connection == null) {
            return false;
        }

        return isStoped(connection.getSource()) && isStoped(connection.getDestination())//
                && !connection.getFlowFileQueue().isEmpty();
    }

    private boolean isStoped(final Connectable node) {
        final ScheduledState state = node.getScheduledState();
        if (ScheduledState.DISABLED.equals(state) || ScheduledState.STOPPED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @apiNote 获取Group下所有可以清除数据的connection的ID
     * @param groupId
     * @param connIdsSet
     */
    private void getConnectionIdsFromGroup(String groupId, Set<String> connIdsSet) {
        final ProcessGroup group = flowController.getGroup(groupId);
        if (group == null) {
            return;
        }
        for (Connection connection : group.getConnections()) {
            if (isCanClearQueue(connection)) {
                connIdsSet.add(connection.getIdentifier());
            }
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            getConnectionIdsFromGroup(childGroup.getIdentifier(), connIdsSet);
        }
    }

}
