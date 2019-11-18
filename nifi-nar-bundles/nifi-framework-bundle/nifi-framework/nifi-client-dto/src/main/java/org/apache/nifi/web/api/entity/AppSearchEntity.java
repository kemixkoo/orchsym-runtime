package org.apache.nifi.web.api.entity;

import java.util.Set;

/**
 */
public class AppSearchEntity extends Entity {
    private String text; // 搜索文本

    // 排序
    private String sortedField; // 目前支持: name, createdTime, modifiedTime
    private Boolean isDesc; // 升降序排列

    // 过滤
    private Boolean deleted;
    private Boolean enabled;
    private Boolean isRunning;
    private Boolean hasDataQueue;

    private String filterTimeField; // 目前支持: createdTime, modifiedTime
    private Long beginTime; // 起止时间
    private Long endTime;

    private Set<String> tags;

    // 分页
    private Integer currentPage;
    private Integer pageSize;

    private Boolean needDetail; // 详情

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public Boolean getHasDataQueue() {
        return hasDataQueue;
    }

    public void setHasDataQueue(Boolean hasDataQueue) {
        this.hasDataQueue = hasDataQueue;
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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getNeedDetail() {
        return needDetail;
    }

    public void setNeedDetail(Boolean needDetail) {
        this.needDetail = needDetail;
    }

}
