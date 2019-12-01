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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.orchsym.DataPage;
import org.apache.nifi.web.api.orchsym.OrchsymSearchEntity;

/**
 * 增强模板 查询实体
 *
 * @author liuxun
 */
public class TemplateSearchEntity extends OrchsymSearchEntity {
    /**
     * 排序或筛选 相关的时间字段 createdTime modifiedTime uploadedTime
     */
    private static final String CREATED_TIME_FIELD = "CREATEDTIME";
    private static final String MODIFIED_TIME_FIELD = "MODIFIEDTIME";
    private static final String UPLOADED_TIME_FIELD = "UPLOADEDTIME";

    private static final String NAME_FIELD = "NAME";

    private String sortedField = CREATED_TIME_FIELD; // name, createdTime modifiedTime uploadedTime
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * 模板类型
     */
    private String templateType;
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

    private String filterTimeField; // createdTime modifiedTime uploadedTime
    private Long beginTime;
    private Long endTime;
    /**
     * 标签
     */
    private List<String> tags;

    public String getSortedField() {
        return sortedField;
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
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

    public String getUploadedUserId() {
        return uploadedUserId;
    }

    public void setUploadedUserId(String uploadedUserId) {
        this.uploadedUserId = uploadedUserId;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    /**
     * 根据自身对象的条件进行筛选，如果相应参数未设置，则忽略过滤而保留
     *
     * @param list
     *            待筛选的数组
     * @return
     */
    public DataPage<TemplateDTO> getTempsByFilter(List<TemplateDTO> list) {
        return getTempsByFilter(list, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
    }

    public DataPage<TemplateDTO> getTempsByFilter(List<TemplateDTO> list, final Comparator<String> nameComparator) {
        // 过滤筛选 + 排序
        final List<TemplateDTO> filterList = list.stream().filter(t -> {
            String text = this.getText();
            if (StringUtils.isNotBlank(text) //
                    && !contains(text, new String[] { //
                            t.getName(), //
                            t.getDescription(), //
                            Objects.isNull(t.getTags()) ? "" : String.join(",", t.getTags())//
            })) {
                return false;
            }
            if (this.getTags() != null && t.getTags() != null && !t.getTags().containsAll(this.getTags())) {
                return false;
            }

            if (StringUtils.isNotBlank(this.getTemplateType()) && TemplateType.match(this.getTemplateType()).not(t.getAdditions())) {
                return false;
            }
            if (StringUtils.isNotBlank(this.getSourceType()) && TemplateSourceType.match(this.getSourceType()).not(t.getAdditions())) {
                return false;
            }

            final TemplateAdditions additions = getAdditions(t);
            if (additions.isDeleted() != this.isDeleted()) {
                return false;
            }

            if (StringUtils.isNotBlank(this.getCreatedUserId()) && !this.getCreatedUserId().equals(additions.getCreatedUser())) {
                return false;
            }

            if (StringUtils.isNotBlank(this.getModifiedUserId()) && !this.getModifiedUserId().equals(additions.getModifiedUser())) {
                return false;
            }

            if (StringUtils.isNotBlank(this.getUploadedUserId()) && !this.getUploadedUserId().equals(additions.getUploadedUser())) {
                return false;
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
                break;
            default:
                break;
            }
            if (time1 != null && time2 != null) {
                final int compare = time2.compareTo(time1);
                return isDesc() ? compare : -compare;
            } else if (time1 != null) {
                return isDesc() ? -1 : 1;
            } else if (time2 != null) {
                return isDesc() ? 1 : -1;
            } else {
                final int compare = nameComparator.compare(o2.getName(), o1.getName());
                return isDesc() ? compare : -compare;
            }
        }).collect(Collectors.toList());

        return new DataPage<>(filterList, this.getPageSize(), this.getPage());
    }

    private TemplateAdditions getAdditions(TemplateDTO dto) {
        return new TemplateAdditions(dto);
    }

}
