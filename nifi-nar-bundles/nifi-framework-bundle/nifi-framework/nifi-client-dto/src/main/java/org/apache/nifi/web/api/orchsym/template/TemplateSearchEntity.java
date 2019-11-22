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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.orchsym.DataPage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 增强模板 查询实体
 *
 * @author liuxun
 */
public class TemplateSearchEntity {
    /**
     * 排序或筛选 相关的时间字段 createdTime modifiedTime uploadedTime
     */
    private static final String CREATED_TIME_FIELD = "CREATEDTIME";
    private static final String MODIFIED_TIME_FIELD = "MODIFIEDTIME";
    private static final String UPLOADED_TIME_FIELD = "UPLOADEDTIME";

    private static final String NAME_FIELD = "NAME";

    /**
     * 检索关键字(在名称和备注中检索 或的关系)
     */
    private String text = "";
    /**
     * 当前页 默认是第一页
     */
    private Integer page = 1;
    private Integer PageSize = 10;
    private String sortedField = CREATED_TIME_FIELD;
    private Boolean isDesc = true;
    /**
     * 来源类型
     */
    private Integer sourceType;
    /**
     * 根据创建者用户ID筛选
     */
    private String createdUserId;
    /**
     * 根据修改者ID筛选
     */
    private String modifiedUserId;
    /**
     * 根据上传者ID筛选
     */
    private String uploadedUserId;

    private String filterTimeField;
    private Long beginTime;
    private Long endTime;
    private List<String> tags;

    private boolean deleted = false;
    private Integer templateType;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getSortedField() {
        return sortedField;
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    public Boolean getIsDesc() {
        return isDesc;
    }

    public void setIsDesc(Boolean isDesc) {
        this.isDesc = isDesc;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
    }

    public String getModifiedUserId() {
        return modifiedUserId;
    }

    public void setModifiedUserId(String modifiedUserId) {
        this.modifiedUserId = modifiedUserId;
    }

    public String getFilterTimeField() {
        return filterTimeField;
    }

    public void setFilterTimeField(String filterTimeField) {
        this.filterTimeField = filterTimeField;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getPageSize() {
        return PageSize;
    }

    public void setPageSize(Integer pageSize) {
        PageSize = pageSize;
    }

    public String getUploadedUserId() {
        return uploadedUserId;
    }

    public void setUploadedUserId(String uploadedUserId) {
        this.uploadedUserId = uploadedUserId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    /**
     * 根据自身对象的条件进行筛选
     *
     * @param list 待筛选的数组
     * @return
     */
    public DataPage<TemplateDTO> getTempsByFilter(List<TemplateDTO> list) {
        // 过滤筛选 + 排序
        final List<TemplateDTO> filterList = list.stream().filter(t -> {
            final String q = this.getText();
            if (!StringUtils.isBlank(q)) {
                if (!(t.getName().contains(q) || t.getDescription().contains(q))) {
                    return false;
                }
            }
            if (this.tags != null && !this.tags.isEmpty()) {
                if (!t.getTags().containsAll(this.tags)) {
                    return false;
                }
            }

            final TemplateAdditions additions = getAdditions(t);
            if (additions.isDeleted() != this.isDeleted()) {
                return false;
            }

            if (additions.getTemplateType() != null && !additions.getTemplateType().equals(this.getTemplateType())) {
                return false;
            }

            final String createdUserId = this.getCreatedUserId();
            if (!StringUtils.isBlank(createdUserId)) {
                if (!createdUserId.equals(additions.getCreatedUser())) {
                    return false;
                }
            }

            final String modifiedUserId = this.getModifiedUserId();
            if (!StringUtils.isBlank(modifiedUserId)) {
                if (!modifiedUserId.equals(additions.getModifiedUser())) {
                    return false;
                }
            }

            final String uploadedUserId = this.getUploadedUserId();
            if (!StringUtils.isBlank(uploadedUserId)) {
                if (!uploadedUserId.equals(additions.getUploadedUser())) {
                    return false;
                }
            }

            // 按照时间筛选
            boolean isTimeFilter = this.getFilterTimeField() != null && (this.getBeginTime() != null || this.getEndTime() != null);
            if (isTimeFilter) {
                Long time = null;
                switch (this.getFilterTimeField().toUpperCase()) {
                    case CREATED_TIME_FIELD:
                        time = additions.getCreatedTime();
                        break;
                    case MODIFIED_TIME_FIELD:
                        time = additions.getModifiedTime();
                        break;
                    case UPLOADED_TIME_FIELD:
                        time = additions.getUploadedTime();
                        break;
                    default:
                        break;
                }
                if (time != null) {
                    if (this.beginTime != null && time < beginTime) {
                        return false;
                    }
                    if (this.endTime != null && time > endTime) {
                        return false;
                    }
                }
            }

            return true;
        }).sorted((o1, o2) -> {
            Long time1 = null;
            Long time2 = null;
            final Boolean isDesc = getIsDesc() == null ? true : getIsDesc();
            switch (getSortedField().toUpperCase()) {
                case CREATED_TIME_FIELD:
                    time1 = getAdditions(o1).getCreatedTime();
                    time2 = getAdditions(o2).getCreatedTime();
                    break;
                case MODIFIED_TIME_FIELD:
                    time1 = getAdditions(o1).getModifiedTime();
                    time2 = getAdditions(o2).getModifiedTime();
                    break;
                case UPLOADED_TIME_FIELD:
                    time1 = getAdditions(o1).getUploadedTime();
                    time2 = getAdditions(o2).getModifiedTime();
                    break;
                case NAME_FIELD:
                    time1 = time2 = null;
                default:
                    break;
            }
            if (time1 != null && time2 != null) {
                return isDesc ? time2.compareTo(time1) : time1.compareTo(time2);
            } else {
                return isDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
            }
        }).collect(Collectors.toList());

        return new DataPage<>(filterList, this.getPageSize(), this.getPage());
    }

    private TemplateAdditions getAdditions(TemplateDTO dto) {
        return new TemplateAdditions(dto);
    }

}
