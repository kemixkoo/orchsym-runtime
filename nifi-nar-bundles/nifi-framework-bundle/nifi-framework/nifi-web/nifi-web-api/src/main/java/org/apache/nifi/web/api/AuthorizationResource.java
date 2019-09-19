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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.AccessDeniedException;
import org.apache.nifi.authorization.AuthorizationResult;
import org.apache.nifi.authorization.AuthorizationResult.Result;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.RootGroupPortAuthorizable;
import org.apache.nifi.authorization.SnippetAuthorizable;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.resource.ResourceType;
import org.apache.nifi.authorization.user.NiFiUser;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author GU Guoqiang
 *
 */
@Component
@Path(AuthorizationResource.PATH)
@Api(value = AuthorizationResource.PATH, description = "Endpoint for check the authorization")
public class AuthorizationResource extends AbsOrchsymResource {
    static final String PATH = "/auth";

    /**
     * Retrieves the Authorization.
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{action}")
    @ApiOperation(value = "Gets the authorization")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getAuthorizable(//
            @ApiParam(value = "The type of auth", required = true) @PathParam("type") String type, //
            @ApiParam(value = "The operation action for read/write", required = true) @PathParam("action") String action, //
            @ApiParam(value = "The id of connection") @QueryParam("id") final String resourceId) {

        final ResourceType requestType = ResourceType.valueOfValue("/" + type.toLowerCase());
        final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

        String resourcePath = requestType.getValue();
        if (StringUtils.isNotBlank(resourceId)) {
            // Support : ControllerService, Funnel, Label, InputPort, OutputPort, Processor, ProcessGroup, RemoteProcessGroup, ReportingTask, Template
            resourcePath += '/' + resourceId;
        } // else { // Support: Controller, Counters, Flow, Provenance, Proxy, Policy, Resource, SiteToSite, System, Tenant

        //
        final String resource = resourcePath;

        serviceFacade.authorizeAccess(lookup -> {
            Authorizable authorizable = null;
            if (ResourceType.RestrictedComponents == requestType) { // don't support in getAuthorizableFromResource
                authorizable = lookup.getRestrictedComponents();
            } else {
                authorizable = lookup.getAuthorizableFromResource(resource);
            }

            if (authorizable == null) {
                throw new IllegalArgumentException("An unexpected type of resource in this policy " + requestType.getValue());
            }

            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            authorizable.authorize(authorizer, requestAction, user);
        });

        return Response.ok().build();
    }

    /**
     * Retrieves the Authorization for Connection
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/connection/{id}/{action}")
    @ApiOperation(value = "Gets the authorization")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getConnectionAuthorizable(//
            @ApiParam(value = "The id of connection", required = true) @PathParam("id") final String connectionId, //
            @ApiParam(value = "The operation action for read/write", required = true) @PathParam("action") final String action) {
        final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

        serviceFacade.authorizeAccess(lookup -> {
            final Authorizable authorizable = lookup.getConnection(connectionId).getAuthorizable();

            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            authorizable.authorize(authorizer, requestAction, user);
        });

        return Response.ok().build();
    }

    /**
     * Retrieves the Authorization for Snippet
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/snippet/{id}/{action}")
    @ApiOperation(value = "Gets the authorization")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getSnippetAuthorizable(//
            @ApiParam(value = "The id of snippet", required = true) @PathParam("id") final String snippetId, //
            @ApiParam(value = "The operation action for read/write", required = true) @PathParam("action") final String action) {
        final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

        serviceFacade.authorizeAccess(lookup -> {
            final SnippetAuthorizable snippet = lookup.getSnippet(snippetId);

            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            snippet.getParentProcessGroup().authorize(authorizer, requestAction, user);

            authorizeSnippet(snippet, authorizer, lookup, requestAction, true, false);

        });

        return Response.ok().build();
    }

    /**
     * Retrieves the Authorization for root input/output port
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/root-port/{type}/{id}")
    @ApiOperation(value = "Gets the authorization")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getRootPortAuthorizable(//
            @ApiParam(value = "The type of auth", required = true) @PathParam("type") final String type, //
            @ApiParam(value = "The id of root port", required = true) @PathParam("id") final String portId) {
        final boolean input = "input".equals(type.toLowerCase());
        final boolean output = "output".equals(type.toLowerCase());
        if (!input && !output) {
            throw new IllegalArgumentException("The resource must be an Input or Output Port.");
        }

        serviceFacade.authorizeAccess(lookup -> {
            RootGroupPortAuthorizable authorizable = null;
            if (input) {
                authorizable = lookup.getRootGroupInputPort(portId);
            } else if (output) {
                authorizable = lookup.getRootGroupOutputPort(portId);
            }

            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            final AuthorizationResult authorizationResult = authorizable.checkAuthorization(user);
            if (!Result.Approved.equals(authorizationResult.getResult())) {
                throw new AccessDeniedException(authorizationResult.getExplanation());
            }
            // authorizable.authorize(authorizer, requestAction, user); //?
        });

        return Response.ok().build();
    }

    /**
     * Retrieves the Authorization for Access Policy
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/policy/{id}/{action}")
    @ApiOperation(value = "Gets the authorization")
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response getAccessPolicyAuthorizable(//
            @ApiParam(value = "The id of access", required = true) @PathParam("id") final String accessId, //
            @ApiParam(value = "The operation action for read/write", required = true) @PathParam("action") final String action) {
        final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

        serviceFacade.authorizeAccess(lookup -> {
            // final Authorizable authorizable = lookup.getAccessPolicyByResource(resource);
            final Authorizable authorizable = lookup.getAccessPolicyById(accessId);

            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            authorizable.authorize(authorizer, requestAction, user);
        });

        return Response.ok().build();
    }

}
