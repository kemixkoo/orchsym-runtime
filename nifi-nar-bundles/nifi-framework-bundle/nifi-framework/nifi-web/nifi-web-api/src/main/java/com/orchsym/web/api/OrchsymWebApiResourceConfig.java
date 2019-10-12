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
package com.orchsym.web.api;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.nifi.web.api.AbsOrchsymResource;
import org.apache.nifi.web.api.filter.RedirectResourceFilter;
import org.apache.nifi.web.util.ObjectMapperResolver;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author GU Guoqiang
 *
 */
public class OrchsymWebApiResourceConfig extends ResourceConfig {

    public OrchsymWebApiResourceConfig(@Context ServletContext servletContext) {
        // get the application context to register the rest endpoints
        final ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        // request support
        register(RedirectResourceFilter.class);
        register(MultiPartFeature.class);

        // jackson
        register(JacksonFeature.class);
        register(ObjectMapperResolver.class);

        // rest api
        final Map<String, Object> beansMap = ctx.getBeansWithAnnotation(Component.class);
        for (Entry<String, Object> entry : beansMap.entrySet()) {
            final Object bean = entry.getValue();
            if (AbsOrchsymResource.class.isInstance(bean)) { // make sure for resource
                register(bean);
            }
        }

        // gzip
        EncodingFilter.enableFor(this, GZipEncoder.class);
    }

}
