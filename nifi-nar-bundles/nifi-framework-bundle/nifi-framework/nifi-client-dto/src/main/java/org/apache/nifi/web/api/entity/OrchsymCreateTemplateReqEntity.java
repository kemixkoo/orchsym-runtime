package org.apache.nifi.web.api.entity;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author liuxun
 *         创建模板请求实体类(增强)
 */
@XmlRootElement(name = "OrchsymCreateTemplateReqEntity")
public class OrchsymCreateTemplateReqEntity extends CreateTemplateRequestEntity {
    /**
     * 创建人 (即是谁保存生成的模板)
     * 创建人的identifier
     */
    private String createdUser;
    /**
     * 创建时间
     */
    private Long createdTime;
    private String uploadedUser;
    private Long uploadedTime;
    private String modifiedUser;
    private Long modifiedTime;

    private String sourceType;
    private Set<String> tags;

    /**
     * 表示创建增强模板时，如果有重名，是否删
     */
    private boolean overwrite;

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(Long uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
