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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.nifi.web.api.entity.OrchsymCreateTemplateReqEntity;
import org.apache.nifi.web.api.entity.OrchsymTemplateEntity;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;

/**
 * 负责封装Template 增强过程中持久化字段的名称
 *
 * @author liuxun
 */
public class TemplateFieldName {
    public static final String UPLOADED_USER = "UPLOADED_USER";
    public static final String UPLOADED_TIMESTAMP = "UPLOADED_TIMESTAMP";
    public static final String SOURCE_TYPE = "SOURCE_TYPE";
    public static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";
    public static final String IS_FAVORITE = "IS_FAVORITE";

    public static final String KEY_USER_TEMP_FAV = "USER_TEMPLATES_FAVORITES";

    public static Map<String, String> getCreatedAdditions(OrchsymCreateTemplateReqEntity requestEntity, boolean app, String userId) {

        Map<String, String> additions = new HashMap<>(10);

        // 构建默认设置
        additions.put(AdditionConstants.KEY_CREATED_USER, userId);
        additions.put(AdditionConstants.KEY_CREATED_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        additions.put(TemplateFieldName.TEMPLATE_TYPE, app ? TemplateType.APPLICATION.name() : TemplateType.NORMAL.name());
        additions.put(TemplateFieldName.SOURCE_TYPE, TemplateSourceType.SAVE_AS.name());

        // 由设置覆盖, 主要是时间戳在集群中不一致
        if (!Objects.isNull(requestEntity)) {

            if (!Objects.isNull(requestEntity.getCreatedUser())) {
                additions.put(AdditionConstants.KEY_CREATED_USER, requestEntity.getCreatedUser());
            }
            if (!Objects.isNull(requestEntity.getCreatedTime())) {
                additions.put(AdditionConstants.KEY_CREATED_TIMESTAMP, Long.toString(requestEntity.getCreatedTime()));
            }

            if (!Objects.isNull(requestEntity.getModifiedUser())) {
                additions.put(AdditionConstants.KEY_MODIFIED_USER, requestEntity.getModifiedUser());
            }
            if (!Objects.isNull(requestEntity.getModifiedTime())) {
                additions.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, Long.toString(requestEntity.getModifiedTime()));
            }

            if (!Objects.isNull(requestEntity.getUploadedUser())) {
                additions.put(TemplateFieldName.UPLOADED_USER, requestEntity.getUploadedUser());
            }
            if (!Objects.isNull(requestEntity.getUploadedTime())) {
                additions.put(TemplateFieldName.UPLOADED_TIMESTAMP, Long.toString(requestEntity.getUploadedTime()));
            }
        }

        return additions;
    }

    public static Map<String, String> getUploadedAdditions(OrchsymTemplateEntity requestEntity, String userId) {
        Map<String, String> additions = new HashMap<>();
        additions.put(TemplateFieldName.UPLOADED_USER, userId);
        additions.put(TemplateFieldName.UPLOADED_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        additions.put(TemplateFieldName.SOURCE_TYPE, TemplateSourceType.UPLOADED.name());

        if (!Objects.isNull(requestEntity)) {
            if (!Objects.isNull(requestEntity.getUploadedUser())) {
                additions.put(TemplateFieldName.UPLOADED_USER, requestEntity.getUploadedUser());
            }
            if (!Objects.isNull(requestEntity.getUploadedTime())) {
                additions.put(TemplateFieldName.UPLOADED_TIMESTAMP, Long.toString(requestEntity.getUploadedTime()));
            }
        }
        return additions;
    }
}
