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
package com.baishancloud.orchsym.processors.ui.api;

import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.HttpServletConfigurationRequestContext;
import org.apache.nifi.web.HttpServletRequestContext;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.apache.nifi.web.NiFiWebConfigurationRequestContext;
import org.apache.nifi.web.NiFiWebRequestContext;
import org.apache.nifi.web.Revision;
import org.apache.nifi.web.UiExtensionType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;


class ProcessorWebUtils {

    static ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext,final String processorId,
                                                       final Long revision, final String clientId, HttpServletRequest request){

        final  NiFiWebRequestContext requestContext;

        if(processorId != null && revision != null && clientId != null){
            requestContext = getRequestContext(processorId, revision, clientId, request) ;
        } else{
            requestContext = getRequestContext(processorId,request);
        }

        return configurationContext.getComponentDetails(requestContext);

    }

    static ComponentDetails getComponentDetails(final NiFiWebConfigurationContext configurationContext,final String processorId, HttpServletRequest request){
        return getComponentDetails(configurationContext,processorId,null,null,request);
    }

    static Response.ResponseBuilder applyCacheControl(Response.ResponseBuilder response) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl);
    }

    static NiFiWebConfigurationRequestContext getRequestContext(final String processorId, final Long revision, final String clientId, HttpServletRequest request) {
        return new HttpServletConfigurationRequestContext(UiExtensionType.ProcessorConfiguration, request) {
            @Override
            public String getId() {
                return processorId;
            }

            @Override
            public Revision getRevision() {
                return new Revision(revision, clientId, processorId);
            }
        };
    }


    private static NiFiWebRequestContext getRequestContext(final String processorId, HttpServletRequest request) {
        return new HttpServletRequestContext(UiExtensionType.ProcessorConfiguration, request) {
            @Override
            public String getId() {
                return processorId;
            }
        };
    }


}
