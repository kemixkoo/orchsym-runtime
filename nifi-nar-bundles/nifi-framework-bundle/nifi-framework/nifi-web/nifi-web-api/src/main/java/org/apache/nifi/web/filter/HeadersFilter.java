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
package org.apache.nifi.web.filter;

import org.apache.nifi.logging.NiFiLog;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;
import org.apache.nifi.web.filter.wrapper.HeaderMapRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ThreadLocalUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 增强头信息过滤器，为修改操作请求添加时间戳头信息
 * @author liuxun
 */
public class HeadersFilter implements Filter {
    private static final Logger logger = new NiFiLog(LoggerFactory.getLogger(HeadersFilter.class));
    private static final List<String> METHODS = new ArrayList<>(Arrays.asList("POST", "PUT", "DELETE"));

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        String method = request.getMethod();
        if ("HTTP".equalsIgnoreCase(request.getScheme()) && METHODS.contains(method)) {
            // 避免线程池导致 ThreadLocal重用
            ThreadLocalUtil.getInstance(true).remove();

            long createdTime;
            final String timesFieldName = AdditionConstants.KEY_MODIFIED_TIMESTAMP;
            String timeHeader = request.getHeader(timesFieldName);
            if(timeHeader == null){
                HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
                createdTime = System.currentTimeMillis();
                requestWrapper.addHeader(timesFieldName, Long.toString(createdTime));
                logger.debug("Method:{} ----NewTime:{}", method, createdTime);
                ThreadLocalUtil.getInstance(true).set(createdTime);
                filterChain.doFilter(requestWrapper,resp);
            }else {
                createdTime = Long.parseLong(request.getHeader(timesFieldName));
                logger.debug("Method:{} ----OldTime:{}", method, createdTime);
                ThreadLocalUtil.getInstance(true).set(createdTime);
            }
        }

        filterChain.doFilter(req,resp);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
