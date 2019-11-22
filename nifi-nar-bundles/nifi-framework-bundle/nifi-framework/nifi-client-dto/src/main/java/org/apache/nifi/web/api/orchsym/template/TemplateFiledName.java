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
package org.apache.nifi.web.api.orchsym.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;

/**
 * 负责封装Template 增强过程中持久化字段的名称
 *
 * @author liuxun
 */
public class TemplateFiledName {
    public static final String UPLOADED_USER = "UPLOADED_USER";
    public static final String UPLOADED_TIMESTAMP = "UPLOADED_TIMESTAMP";
    public static final String SOURCE_TYPE = "SOURCE_TYPE";
    public static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";

    private static Set<String> FIELDNAMES = new HashSet<>();

    static {
        FIELDNAMES.addAll(Arrays.asList(AdditionConstants.KEY_CREATED_USER, AdditionConstants.KEY_CREATED_TIMESTAMP, UPLOADED_USER, UPLOADED_TIMESTAMP, AdditionConstants.KEY_MODIFIED_TIMESTAMP,
                AdditionConstants.KEY_MODIFIED_USER, SOURCE_TYPE));
    }

    public Boolean containsFieldName(String fieldName) {
        return FIELDNAMES.contains(fieldName);
    }
}
