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
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.nifi.connectable.Connectable;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.entity.ConnectionStatusEntity;
import org.apache.nifi.web.api.entity.QueueSnippetEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;

/**
 * @author liuxun
 * @apiNote 处理connections清除队列的相关功能
 */
@Component
@Path("/queues")
@Api(value = "/queues", description = "clear queues of relational connections")
public class QueueResource extends AbsOrchsymResource {

    @Autowired
    private FlowFileQueueResource flowFileQueueResource;

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response getConnectionsStatus(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The selected queue and groups.", required = true) final QueueSnippetEntity queueSnippetEntity) {
        return collect(queueSnippetEntity, false);
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/clean")
    public Response clean(@Context final HttpServletRequest httpServletRequest, //
            @ApiParam(value = "The selected queue and groups.", required = true) final QueueSnippetEntity queueSnippetEntity) {
        return collect(queueSnippetEntity, true);
    }

    protected Response collect(final QueueSnippetEntity queueSnippetEntity, boolean clean) {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.POST, queueSnippetEntity);
        } else if (isDisconnectedFromCluster()) {
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
        if (clean) {
            // drop flowFile in queue of connection
            for (String waitClearConnId : canClearQueueConnIds) {
                dropFlowFilesInConn(waitClearConnId);
            }
        }
        JSONObject result = new JSONObject();
        result.put("connections", canClearQueueConnIds);
        return noCache(Response.ok(result)).build();
    }

    // private methods

    /**
     * @param connectionId
     * @return
     * @apiNote 判断是否可以清除指定连接的队列数据
     */
    private Boolean isCanClearQueue(String connectionId) {
        final Connection connection = flowController.getConnection(connectionId);
        if (connection == null) {
            return false;
        }

        if (isNotRunning(connection.getSource()) && isNotRunning(connection.getDestination())) { // both don't run
            final ConnectionStatusEntity entity = serviceFacade.getConnectionStatus(connectionId);
            if (entity.getConnectionStatus().getAggregateSnapshot().getFlowFilesQueued() > 0) { // have queue
                return true;
            }
        }
        return false;
    }

    private boolean isNotRunning(final Connectable node) {
        final ScheduledState state = node.getScheduledState();
        if (ScheduledState.STARTING.equals(state) || ScheduledState.RUNNING.equals(state)) {
            return false;
        }
        return true;
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
            if (isCanClearQueue(connection.getIdentifier())) {
                connIdsSet.add(connection.getIdentifier());
            }
        }

        for (ProcessGroup childGroup : group.getProcessGroups()) {
            getConnectionIdsFromGroup(childGroup.getIdentifier(), connIdsSet);
        }
    }

    /**
     *
     * @apiNote 清除指定连接中的数据
     * @param connId
     *            连接组件的ID
     */
    private void dropFlowFilesInConn(String connId) {
        DropRequestDTO dropRequest = serviceFacade.createFlowFileDropRequest(connId, UUID.randomUUID().toString());
        serviceFacade.deleteFlowFileDropRequest(connId, dropRequest.getId());
    }
}
