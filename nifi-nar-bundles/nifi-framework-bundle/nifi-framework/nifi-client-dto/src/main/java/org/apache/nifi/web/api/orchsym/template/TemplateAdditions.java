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

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;

import java.util.Map;

/**
 * @apiNote 用于从additions中 提取template的增强信息
 * @author liuxun
 */
public class TemplateAdditions {
    private String createdUser;
    private Long createdTime;
    private String uploadedUser;
    private Long uploadedTime;
    private String modifiedUser;
    private Long modifiedTime;
    private Integer sourceType;

    public TemplateAdditions() {
    }

    public TemplateAdditions(TemplateDTO dto) {
        final Map<String, String> map = dto.getAdditions();
        if (map != null && !map.isEmpty()) {
            if (map.containsKey(AdditionConstants.KEY_CREATED_USER)) {
                this.setCreatedUser(map.get(AdditionConstants.KEY_CREATED_USER));
            }
            if (map.containsKey(AdditionConstants.KEY_CREATED_TIMESTAMP)) {
                this.setCreatedTime(Long.parseLong(map.get(AdditionConstants.KEY_CREATED_TIMESTAMP)));
            } else {
                this.setCreatedTime(dto.getTimestamp().getTime());
            }
            if (map.containsKey(AdditionConstants.KEY_MODIFIED_USER)) {
                this.setModifiedUser(map.get(AdditionConstants.KEY_MODIFIED_USER));
            }
            if (map.containsKey(AdditionConstants.KEY_MODIFIED_TIMESTAMP)) {
                this.setModifiedTime(Long.parseLong(map.get(AdditionConstants.KEY_MODIFIED_TIMESTAMP)));
            }
            if (map.containsKey(TemplateFiledName.UPLOADED_USER)) {
                this.setUploadedUser(map.get(TemplateFiledName.UPLOADED_USER));
            }
            if (map.containsKey(TemplateFiledName.UPLOADED_TIMESTAMP)) {
                this.setUploadedTime(Long.parseLong(map.get(TemplateFiledName.UPLOADED_TIMESTAMP)));
            }
            if (map.containsKey(TemplateFiledName.SOURCE_TYPE)) {
                this.setSourceType(Integer.parseInt(map.get(TemplateFiledName.SOURCE_TYPE)));
            }
        }
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getUploadedUser() {
        return uploadedUser;
    }

    public void setUploadedUser(String uploadedUser) {
        this.uploadedUser = uploadedUser;
    }

    public Long getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(Long uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }
}
