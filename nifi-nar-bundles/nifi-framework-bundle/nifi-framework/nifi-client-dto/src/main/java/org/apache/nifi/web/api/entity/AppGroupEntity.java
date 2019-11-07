package org.apache.nifi.web.api.entity;

/**
 * @apiNote 所有的一级group
 */
public class AppGroupEntity {
    private String id;
    private String name;
    private String comments;
    private Long createdTime;
    private Long modifiedTime;
    private Boolean isDeleted;

    public AppGroupEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppGroupEntity){
            AppGroupEntity app = (AppGroupEntity) obj;
            return this.id.equals(app.getId());
        }
        return false;
    }

}
