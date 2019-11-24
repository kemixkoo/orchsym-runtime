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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.apache.nifi.authorization.util.MD5Util;
import org.apache.nifi.http.ResultUtil;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.web.api.entity.AuthorizationEntity;
import org.apache.nifi.web.security.jwt.JwtService;
import org.apache.nifi.web.security.token.LoginAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 
 * @author GU Guoqiang
 *
 */
@Component
@Path(OrchsymAuthorizationResource.PATH)
@Api(value = OrchsymAuthorizationResource.PATH, description = "Endpoint for check the authorization")
public class OrchsymAuthorizationResource extends AbsOrchsymResource {
    static final String PATH = "/auth";
    private static final Logger logger = LoggerFactory.getLogger(OrchsymAuthorizationResource.class);

    @Autowired
    private JwtService jwtService;

    /**
     * Retrieves the Authorization.
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        try {
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
                if (ResourceType.RestrictedComponents.equals(requestType)) { // don't support in getAuthorizableFromResource
                    authorizable = lookup.getRestrictedComponents();
                } else if (ResourceType.Policy.equals(requestType)) { // if policy need the sub-resource type
                    authorizable = lookup.getPolicies();
                } else {
                    authorizable = lookup.getAuthorizableFromResource(resource);
                }

                if (authorizable == null) {
                    throw new IllegalArgumentException("An unexpected type of resource in this policy " + requestType.getValue());
                }

                final NiFiUser user = NiFiUserUtils.getNiFiUser();
                authorizable.authorize(authorizer, requestAction, user);
            });
            return generateOkResponse(ResultUtil.success()).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return generateNotAuthorizedResponse().build();
        }
    }

    /**
     * Retrieves the Authorization for Connection
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        try {
            final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

            serviceFacade.authorizeAccess(lookup -> {
                final Authorizable authorizable = lookup.getConnection(connectionId).getAuthorizable();

                final NiFiUser user = NiFiUserUtils.getNiFiUser();
                authorizable.authorize(authorizer, requestAction, user);
            });

            return generateOkResponse(ResultUtil.success()).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return generateNotAuthorizedResponse().build();
        }
    }

    /**
     * Retrieves the Authorization for Snippet
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        try {
            final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

            serviceFacade.authorizeAccess(lookup -> {
                final SnippetAuthorizable snippet = lookup.getSnippet(snippetId);

                final NiFiUser user = NiFiUserUtils.getNiFiUser();
                snippet.getParentProcessGroup().authorize(authorizer, requestAction, user);

                authorizeSnippet(snippet, authorizer, lookup, requestAction, true, false);

            });

            return generateOkResponse(ResultUtil.success()).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return generateNotAuthorizedResponse().build();
        }
    }

    /**
     * Retrieves the Authorization for root input/output port
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        try {
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

            return generateOkResponse(ResultUtil.success()).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return generateNotAuthorizedResponse().build();
        }
    }

    /**
     * Retrieves the Authorization for Access Policy
     *
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        try {
            final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

            serviceFacade.authorizeAccess(lookup -> {
                // final Authorizable authorizable = lookup.getAccessPolicyByResource(resource);
                final Authorizable authorizable = lookup.getAccessPolicyById(accessId);

                final NiFiUser user = NiFiUserUtils.getNiFiUser();
                authorizable.authorize(authorizer, requestAction, user);
            });

            return generateOkResponse(ResultUtil.success()).build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return generateNotAuthorizedResponse().build();
        }
    }

    /**
     * Retrieves the Authorization for Access Policy
     *
     */
    @POST
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/token")
    @ApiOperation(value = "Gets the authorization")
    public Response getToken(@RequestBody AuthorizationEntity auth) {
        // same AccessResource for token to generate
        if (httpServletRequest.isSecure()) {
            final NiFiUser user = NiFiUserUtils.getNiFiUser();
            if (user == null) {
                throw new AccessDeniedException("No user authenticated in the request.");
            }
            if (user.getIdentity().equals(auth.getUsername()) || user.getIdentity().equals(auth.getIdentity())) {

                LoginAuthenticationToken loginToken = new LoginAuthenticationToken(auth.getUsername(), auth.getUsername(), auth.getExpiration(), auth.getIssuer());
                String jwtToken = jwtService.generateSignedToken(loginToken);

                // build the response
                final URI uri = URI.create(generateResourceUri("auth", "token"));
                return generateCreatedResponse(uri, jwtToken).build();
            }

        }
        return generateNotAuthorizedResponse().build();

    }

    @POST
    @Consumes(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Produces(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/external/token")
    @ApiOperation(value = "Generate access token for external app.", notes = NON_GUARANTEED_ENDPOINT)
    public Response getExtAccessToken(Map<String,Object> accessParam) {
        try {
            NiFiProperties nifiProperties = org.apache.nifi.util.NiFiProperties.createBasicNiFiProperties(null, null);
            String username = nifiProperties.getProperty("orchsym.external.access.token.username");
            String pk = nifiProperties.getProperty("orchsym.external.access.token.private.key");
            int exp_time = Integer.parseInt(nifiProperties.getProperty("orchsym.external.access.token.expires.second")) * 1000;// millisecond
            String appid = String.valueOf(accessParam.get("appid"));
            String pid = String.valueOf(accessParam.get("pid"));
            Long time = Long.parseLong(String.valueOf(accessParam.get("time")));
            if (accessParam == null || appid == null || pid == null || time == null) {
                return generateOkResponse(ResultUtil.error("Parameter not valid!")).build();
            }
            long nowTime = System.currentTimeMillis();
            if (nowTime - time > exp_time) {
                return generateOkResponse(ResultUtil.timeout("Request expires!")).build();
            }
            String targetPK = MD5Util.MD5(pk + appid + time);
            if (!targetPK.equalsIgnoreCase(pid)) {
                return generateOkResponse(ResultUtil.error("Invalid request!")).build();
            }
            final LoginAuthenticationToken loginToken = new LoginAuthenticationToken(username, username, exp_time, getClass().getSimpleName());
            String jwtToken = jwtService.generateSignedToken(loginToken);
            logger.debug("jwt token is " + jwtToken);
            return generateOkResponse(ResultUtil.success(jwtToken)).build();
        } catch (Exception e) {
            logger.error("Throw exception when trying to get external access token", e);
            return generateOkResponse(ResultUtil.error("Do not support external access token!")).build();
        }
    }
}
