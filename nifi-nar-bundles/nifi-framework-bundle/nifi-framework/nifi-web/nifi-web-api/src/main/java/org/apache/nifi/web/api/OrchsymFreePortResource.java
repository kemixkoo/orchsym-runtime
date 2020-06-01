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

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.nifi.cluster.coordination.http.replication.AsyncClusterResponse;
import org.apache.nifi.web.util.HostPortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liuxun
 * @apiNote 获取集群中空闲端口号(保证获取的端口号在每台节点都未被占用)
 */
@Path("/free_port")
@Component
public class OrchsymFreePortResource extends AbsOrchsymResource {
    private static final Logger logger = LoggerFactory.getLogger(OrchsymFreePortResource.class);

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check/{port}")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response checkFreePort(@PathParam("port") Integer port) throws UnknownHostException {
        if (isReplicateRequest()) {
            return replicate(HttpMethod.GET);
        }

        if (HostPortUtils.isPortUsing(getAbsolutePath().getHost(), port)) {
            return Response.status(400).build();
        }
        return Response.ok().build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/acquire/{num}/{min}/{max}")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getFreePorts(@PathParam("min") Integer min,
                                 @PathParam("max") Integer max,
                                 @PathParam("num") Integer num
    ) throws InterruptedException, UnknownHostException {
        if (min < 1 || max > 65535 || num < 0) {
            throw new IllegalArgumentException("端口范围只能在1~65535中取整");
        }

        if (!properties.isNode()) {
            Set<Integer> localFreePorts = getLocalFreePorts(min, max, num);
            if (localFreePorts.isEmpty()) {
                throw new IllegalStateException("无效的查询范围, 请改变范围后重试 ");
            }
            return Response.ok(localFreePorts).build();
        }

        if (!isConnectedToCluster()) {
            throw new IllegalStateException("当前节点已断开和集群的连接");
        }

        Set<Integer> resultSet = new HashSet<>();
        try {
            for (int p = min; p <= max; p++) {
                final URI uri = buildResourceUri("free_port", "check", Integer.toString(p));
                final AsyncClusterResponse response = this.requestReplicator.replicate(HttpMethod.GET, uri, getRequestParameters(), getHeaders());
                final Response clientResponse = response.awaitMergedResponse().getClientResponse();
                if (clientResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                    if (resultSet.size() < num) {
                        resultSet.add(p);
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取空闲端口失败", e);
        }

        if (resultSet.isEmpty()) {
            throw new IllegalStateException("无效的查询范围, 请改变范围后重试 ");
        }
        return Response.ok(resultSet).build();
    }

    /**
     * 获取指定范围 指定个数的本地空闲端口
     *
     * @param min
     * @param max
     * @param num
     * @return
     */
    private Set<Integer> getLocalFreePorts(Integer min, Integer max, Integer num) throws UnknownHostException {
        Set<Integer> ports = new HashSet<>();
        for (int i = min; i <= max; i++) {
            final boolean using = HostPortUtils.isPortUsing(getAbsolutePath().getHost(), i);
            if (!using) {
                ports.add(i);
            }
            if (ports.size() == num) {
                break;
            }
        }
        return ports;
    }

}
