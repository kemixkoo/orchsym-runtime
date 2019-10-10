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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.resource.ResourceType;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.util.HttpRequestUtil;
import org.apache.nifi.util.HttpRequestUtil.HttpHeader;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.web.NiFiServiceFacade;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author GU Guoqiang
 *
 */
public abstract class AbsOrchsymResource extends ApplicationResource implements ICodeMessages {
    private static final Logger logger = LoggerFactory.getLogger(AbsOrchsymResource.class);

    @Autowired(required = false)
    protected NiFiServiceFacade serviceFacade;

    @Autowired(required = false)
    protected Authorizer authorizer;

    /**
     * Authorizes access.
     */
    protected void authorizes(final String type, final String action, final String id) {
        if (null == serviceFacade || null == authorizer) {
            try {
                // GET nifi-api/auth/{type}/{action}?id=xxxx
                Map<String, Object> queryParam = new HashMap<String, Object>();
                if (StringUtils.isNoneBlank(id)) {
                    queryParam.put("id", id);
                }
                final HttpResponse response = doNifiApiRequest(HttpGet.METHOD_NAME, String.format("/auth/%s/%s", type, action), null, queryParam, null);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    logger.warn("Access the  /{}/{} have error code {} for reason {} with the response:\n {} ", type, action, response.getStatusLine().getStatusCode(),
                            response.getStatusLine().getReasonPhrase(), HttpRequestUtil.response(response));
                    throw new IllegalArgumentException("No right to access");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format("Unsupport the authorization for %s with %s operation", type, action));
            }

        } else {
            String typeValue = type;

            if (!type.startsWith("/")) {
                typeValue += '/' + type;
            }
            final ResourceType resourceType = ResourceType.valueOfValue(typeValue);

            final RequestAction requestAction = RequestAction.valueOfValue(action.toLowerCase());

            serviceFacade.authorizeAccess(lookup -> {
                String resource = resourceType.getValue();
                if (StringUtils.isNoneBlank(id)) {
                    resource += '/' + id;
                }
                final Authorizable authorizable = lookup.getAuthorizableFromResource(resource);
                authorizable.authorize(authorizer, requestAction, NiFiUserUtils.getNiFiUser());
            });
        }
    }

    private static final List<String> REQ_HEADERS = Collections.unmodifiableList(Arrays.asList(//
            "Authorization", //
            "Locale", //
            "User-Agent"//
    ));

    protected HttpResponse doNifiApiJSONBodyRequest(final String method, String path, String payload, Set<HttpHeader> headers) throws IOException {
        if (null == headers) {
            headers = new HashSet<>();
        } else {
            headers = new HashSet<>(headers);
        }
        headers.add(new HttpHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        return doNifiApiRequest(method, path, payload, headers);
    }

    protected HttpResponse doNifiApiRequest(final String method, String path, String payload, Set<HttpHeader> headers) throws IOException {
        return doNifiApiRequest(method, path, payload, Collections.emptyMap(), headers);
    }

    protected HttpResponse doNifiApiRequest(final String method, String path, String payload, Map<String, Object> queryParam, Set<HttpHeader> headers) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        final String[] segs = path.split("/");
        final String url = generateNifiApiLocalResourceUri(null, (Map<String, Object>) queryParam, segs);

        StringEntity entity = null;
        if (!StringUtils.isBlank(payload)) {
            entity = new StringEntity(payload);
        }

        Set<HttpHeader> requestHeader;
        if (null == headers) {
            requestHeader = new HashSet<>();
        } else {
            requestHeader = new HashSet<>(headers);
        }
        if (null != httpServletRequest) {
            final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                final String headerName = headerNames.nextElement();
                final String headerValue = httpServletRequest.getHeader(headerName);
                if (REQ_HEADERS.contains(headerName)) {
                    requestHeader.add(new HttpHeader(headerName, headerValue));
                }
            }
        }

        final HttpResponse response = HttpRequestUtil.request(method, url, entity, requestHeader);
        return response;
    }

    protected String generateNifiApiLocalResourceUri(String server, final String... path) {
        return generateNifiApiLocalResourceUri(server, (Map<String, Object>) null, path);
    }

    protected String generateNifiApiLocalResourceUri(String server, Map<String, Object> queryParam, final String... path) {
        // use local only, not same as request.
        List<String> newPath = new ArrayList<>();
        if (null != path && path.length > 0) {
            newPath.addAll(Arrays.asList(path));
        }
        if (newPath.size() > 0) {
            newPath.add(0, "nifi-api");
        } else {
            newPath.add("nifi-api");
        }

        return generateLocalResourceUrl(server, queryParam, newPath.toArray(new String[0]));
    }

    protected String generateLocalResourceUrl(String server, Map<String, Object> queryParam, final String... path) {
        NiFiProperties settings = this.properties;
        if (null == settings) {
            settings = NiFiProperties.createBasicNiFiProperties(null, null);
        }
        final String httpHost = settings.getProperty(NiFiProperties.WEB_HTTP_HOST);
        final Integer httpPort = settings.getIntegerProperty(NiFiProperties.WEB_HTTP_PORT, null);
        final String httpsHost = settings.getProperty(NiFiProperties.WEB_HTTPS_HOST);
        final Integer httpsPort = settings.getIntegerProperty(NiFiProperties.WEB_HTTPS_PORT, null);

        String host = "127.0.0.1";
        int port;
        String scheme;
        if (null != httpPort) { // set for http
            // http
            scheme = "http";
            port = httpPort;
            if (StringUtils.isNotBlank(httpHost)) {
                host = httpHost;
            }
        } else {
            // https
            scheme = "https";
            port = httpsPort;
            if (StringUtils.isNotBlank(httpsHost)) {
                host = httpsHost;
            }
        }

        final UriBuilder uriBuilder = new JerseyUriBuilder().scheme(scheme).host(host).port(port);
        if (null != queryParam) {
            queryParam.forEach((k, v) -> uriBuilder.queryParam(k, v));
        }
        return buildServerPath(uriBuilder, server, path);

    }

    protected String generateNifiApiResourceUri(String server, final String... path) {
        UriBuilder uriBuilder = this.uriInfo.getBaseUriBuilder();
        uriBuilder = uriBuilder.replacePath("/nifi-api/");
        return buildServerPath(uriBuilder, server, path);
    }

    private String buildServerPath(final UriBuilder uriBuilder, String server, final String... path) {
        if (StringUtils.isNotBlank(server)) {
            final String[] serverSetting = server.split(":");

            if (serverSetting.length > 0 && StringUtils.isNotBlank(serverSetting[0])) {
                uriBuilder.host(serverSetting[0]);
            }
            if (serverSetting.length > 1 && StringUtils.isNotBlank(serverSetting[1])) {
                try {
                    uriBuilder.port(Integer.parseInt(serverSetting[1]));
                } catch (NumberFormatException e) {
                    //
                }
            }
        }
        final URI uri = buildResourceUri(uriBuilder, path);
        return uri.toString();
    }

    protected Response generateStringOkResponse(final String entity) {
        return super.generateOkResponse(entity).type(MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }

    protected Response createExceptionResponse(Logger logger, Throwable t) {
        if (null != logger) {
            logger.error(t.getMessage(), t);
        }

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw);) {
            t.printStackTrace(pw);

            return noCache(Response.serverError().entity(sw.toString())).build();
        }
    }

    protected void warnResponse(Logger responseLogger, String path, HttpResponse httpResponse) {
        try {
            responseLogger.warn("Access the  {} have error code {} for reason {} with the response:\n {} ", path, httpResponse.getStatusLine().getStatusCode(),
                    httpResponse.getStatusLine().getReasonPhrase(), HttpRequestUtil.response(httpResponse));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
