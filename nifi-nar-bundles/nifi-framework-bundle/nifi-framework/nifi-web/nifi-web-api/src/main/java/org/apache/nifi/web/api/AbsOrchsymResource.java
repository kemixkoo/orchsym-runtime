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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.web.NiFiServiceFacade;
import org.apache.nifi.web.api.ApplicationResource;
import org.apache.nifi.web.api.ICodeMessages;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author GU Guoqiang
 *
 */
public abstract class AbsOrchsymResource extends ApplicationResource implements ICodeMessages {

    @Autowired
    protected NiFiServiceFacade serviceFacade;

    @Autowired
    protected Authorizer authorizer;

    protected String generateLocalResourceUrl(String server, final String... path) {
        final NiFiProperties properties = NiFiProperties.createBasicNiFiProperties(null, null);
        final String httpHost = properties.getProperty(NiFiProperties.WEB_HTTP_HOST);
        final Integer httpPort = properties.getIntegerProperty(NiFiProperties.WEB_HTTP_PORT, null);
        final String httpsHost = properties.getProperty(NiFiProperties.WEB_HTTPS_HOST);
        final Integer httpsPort = properties.getIntegerProperty(NiFiProperties.WEB_HTTPS_PORT, null);

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
}
