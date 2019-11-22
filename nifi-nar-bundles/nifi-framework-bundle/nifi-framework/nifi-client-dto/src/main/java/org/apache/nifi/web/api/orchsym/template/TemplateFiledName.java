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

/**
 * 负责封装Template 增强过程中持久化字段的名称
 *
 * @author liuxun
 */
public interface TemplateFiledName {
    String UPLOADED_USER = "UPLOADED_USER";
    String UPLOADED_TIMESTAMP = "UPLOADED_TIMESTAMP";
    String SOURCE_TYPE = "SOURCE_TYPE";
    String TEMPLATE_TYPE = "TEMPLATE_TYPE";
    String IS_FAVORITE = "IS_FAVORITE";

    String KEY_USER_TEMP_FAV = "USER_TEMPLATES_FAVORITES";

}
